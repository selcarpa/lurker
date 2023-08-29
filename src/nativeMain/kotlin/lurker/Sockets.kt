package lurker

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.runBlocking
import model.protocol.DnsPackage

object Dns {
    fun startServer(selectorManager: SelectorManager, port: Int = 53) {
        runBlocking {
            val serverSocket = aSocket(selectorManager).udp().bind(InetSocketAddress("0.0.0.0", port))
            println("Echo Server listening at ${serverSocket.localAddress}")
            while (true) {
                val datagram = serverSocket.receive()
                val readBytes = datagram.packet.readBytes()
                println("Accepted ${String(readBytes)}")
                serverSocket.send(Datagram(ByteReadPacket("reply".toByteArray()), datagram.address))
            }
        }
    }

    fun sendADnsRequest(selectorManager: SelectorManager, port: Int = 53) {
        runBlocking {
            val socket = aSocket(selectorManager).udp().connect(InetSocketAddress("8.8.8.8", port))
            val hex = "10210100000100000000000002743406616574686c6902636e00001c0001"
            val byteArray = hex.decodeHex()
            socket.send(
                Datagram(
                    ByteReadPacket(byteArray), socket.remoteAddress
                )
            )
            while (true) {
                val datagram = socket.receive()
                val readBytes = datagram.packet.readBytes()
                println("Accepted ${readBytes.encodeHex()}")

                val dnsPackage = DnsPackage.parse(readBytes)
                println(dnsPackage)
            }
        }
    }

    private fun String.decodeHex(): ByteArray {
        check(length % 2 == 0) { "Must have an even length" }

        return chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()
    }

    private fun ByteArray.encodeHex(): String = joinToString("") {
        it.toInt().and(0xff).toString(16).padStart(2, '0')
    }
}


