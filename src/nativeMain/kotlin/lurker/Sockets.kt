package lurker

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.runBlocking
import model.protocol.DnsPackage
import model.protocol.DnsPackage.Companion.toDnsPackage
import model.protocol.DnsPackage.Companion.toByteArray
import utils.encodeHex

object Dns {
    fun startServer(selectorManager: SelectorManager, port: Int = 53) {
        runBlocking {
            val serverSocket = aSocket(selectorManager).udp().bind(InetSocketAddress("0.0.0.0", port))
            println("Dns Server listening at ${serverSocket.localAddress}")
            while (true) {
                val datagram = serverSocket.receive()
                val readBytes = datagram.packet.readBytes()
                println("Accepted ${String(readBytes)}")
                serverSocket.send(Datagram(ByteReadPacket("reply".toByteArray()), datagram.address))
            }
        }
    }

    fun sendADnsRequest(selectorManager: SelectorManager, port: Int = 53, dnsPackage: DnsPackage) {
        runBlocking {
            val socket = aSocket(selectorManager).udp().connect(InetSocketAddress("8.8.8.8", port))
            val byteArray = dnsPackage.toByteArray()
            socket.send(
                Datagram(
                    ByteReadPacket(byteArray), socket.remoteAddress
                )
            )
            while (true) {
                val datagram = socket.receive()
                val readBytes = datagram.packet.readBytes()
                println("Accepted ${readBytes.encodeHex()}")

                val dnsPackage = readBytes.toDnsPackage()
                println(dnsPackage)
            }
        }
    }

}


