package core

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import model.config.Config.Configuration
import model.protocol.DnsPackage
import model.protocol.DnsPackage.Companion.toByteArray
import model.protocol.DnsPackage.Companion.toDnsPackage
import service.CacheDomainService
import utils.encodeHex
import kotlin.time.Duration.Companion.seconds

private val logger = KotlinLogging.logger {}

object Dns {
    fun startServer(selectorManager: SelectorManager, port: Int = 53) = runBlocking {
        val serverSocket = aSocket(selectorManager).udp().bind(InetSocketAddress("0.0.0.0", port))
        logger.info { ("Dns Server listening at ${serverSocket.localAddress}") }
        while (true) {
            launch {
                val datagram = serverSocket.receive()
                val readBytes = datagram.packet.readBytes()
                val dnsPackage = readBytes.toDnsPackage()

                //TODO: to read cache but not forward the request
                CacheDomainService.get(dnsPackage.question)
                withTimeoutOrNull(Configuration.timeout.seconds) {
                    recursive(selectorManager, dnsPackage, InetSocketAddress("8.8.8.8", 53))
                }.also { recursiveResult ->
                    if (recursiveResult == null) {
                        println("timeout")
                    } else {
                        serverSocket.send(
                            Datagram(
                                ByteReadPacket(recursiveResult.toByteArray()), datagram.address
                            )
                        )
                    }
                }
            }
        }
    }

    fun startTcpServer(selectorManager: SelectorManager, port: Int = 53) = runBlocking {
        val serverSocket = aSocket(selectorManager).tcp().bind(InetSocketAddress("0.0.0.0", port))
        logger.info { ("TCP Dns Server listening at ${serverSocket.localAddress}") }
        while (true) {
            launch {
                val socket = serverSocket.accept()
                val readChannel = socket.openReadChannel()
                val dnsPackage = readChannel.readRemaining().readBytes().toDnsPackage()
                //TODO: to read cache but not forward the request
                withTimeoutOrNull(Configuration.timeout.seconds) {
                    recursive(selectorManager, dnsPackage, InetSocketAddress("8.8.8.8", 53))
                }.also { recursiveResult ->
                    if (recursiveResult == null) {
                        println("timeout")
                    } else {
                        socket.openWriteChannel(autoFlush = true).writeAvailable(recursiveResult.toByteArray())
                    }
                }
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
