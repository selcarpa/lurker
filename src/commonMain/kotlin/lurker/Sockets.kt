package lurker

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import model.config.Config.Configuration
import model.protocol.DnsPackage
import model.protocol.DnsPackage.Companion.toByteArray
import model.protocol.DnsPackage.Companion.toDnsPackage
import utils.Timer
import utils.encodeHex
import kotlin.time.Duration.Companion.seconds

object Dns {

    fun startServer(selectorManager: SelectorManager, port: Int = 53) = runBlocking {
        val serverSocket = aSocket(selectorManager).udp().bind(InetSocketAddress("0.0.0.0", port))
        println("Dns Server listening at ${serverSocket.localAddress}")
        while (true) {
            launch {
                val datagram = serverSocket.receive()
                val readBytes = datagram.packet.readBytes()
                val dnsPackage = readBytes.toDnsPackage()
                //TODO: to read cache but not forward the request
                if (true) {
                    val timer = Timer(Configuration.timeout.seconds, Dispatchers.IO) {
                        println("timeout")
                    }
                    timer.join()
                    val recursive = recursive(selectorManager, dnsPackage, InetSocketAddress("8.8.8.8", 53))
                    timer.cancel()
                    serverSocket.send(
                        Datagram(
                            ByteReadPacket(recursive.toByteArray()), datagram.address
                        )
                    )
                }
            }
        }
    }

    private suspend fun recursive(
        selectorManager: SelectorManager, dnsPackage: DnsPackage, destDns: InetSocketAddress
    ): DnsPackage {
        val socket = aSocket(selectorManager).udp().connect(destDns)
        val byteArray = dnsPackage.toByteArray()
        socket.send(
            Datagram(
                ByteReadPacket(byteArray), socket.remoteAddress
            )
        )
        val datagram = socket.receive()
        val readBytes = datagram.packet.readBytes()
        println("Accepted ${readBytes.encodeHex()}")
        println(readBytes.toDnsPackage())
        socket.close()
        return readBytes.toDnsPackage()
    }
}

