package core

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.encodeToString
import model.config.Config.Configuration
import model.protocol.DnsPackage
import model.protocol.DnsPackage.Companion.toByteArray
import model.protocol.DnsPackage.Companion.toDnsPackage
import service.addCache
import utils.encodeHex
import utils.json
import kotlin.time.Duration.Companion.seconds

private val logger = KotlinLogging.logger {}

object Dns {
    fun startServer(selectorManager: SelectorManager, port: Int = 53) = runBlocking {
        val serverSocket = aSocket(selectorManager).udp().bind(InetSocketAddress("0.0.0.0", port))
        logger.info { ("Dns Server listening at ${serverSocket.localAddress}") }
        while (true) {
            val datagram = serverSocket.receive()
            launch {
                val readBytes = datagram.packet.readBytes()
                val dnsPackage = readBytes.toDnsPackage()

                //TODO: to read cache but not forward the request
                withTimeoutOrNull(Configuration.timeout.seconds) {
                    dnsRequest(selectorManager, dnsPackage, InetSocketAddress("8.8.8.8", 53))
                }.also { recursiveResult ->
                    if (recursiveResult == null) {
                        logger.error { "timeout" }
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
            val socket = serverSocket.accept()
            launch {
                val readChannel = socket.openReadChannel()
                val dnsPackage = readChannel.readRemaining().readBytes().toDnsPackage()
                //TODO: to read cache but not forward the request
                withTimeoutOrNull(Configuration.timeout.seconds) {
                    dnsRequest(
                        selectorManager, dnsPackage, InetSocketAddress(Configuration.recursive.upstream[0].host, 53)
                    )
                }.also { recursiveResult ->
                    if (recursiveResult == null) {
                        logger.warn { "timeout" }
                    } else {
                        socket.openWriteChannel(autoFlush = true).writeAvailable(recursiveResult.toByteArray())
                    }
                }
            }
        }
    }
}

suspend fun dnsRequest(
    selectorManager: SelectorManager, dnsPackage: DnsPackage, destDns: InetSocketAddress
): DnsPackage {
    val socket = aSocket(selectorManager).udp().connect(destDns)
    val byteArray = dnsPackage.toByteArray()
    logger.debug { "dnsRequest send ${byteArray.encodeHex()}" }
    socket.send(
        Datagram(
            ByteReadPacket(byteArray), socket.remoteAddress
        )
    )
    val datagram = socket.receive()
    val readBytes = datagram.packet.readBytes()
    logger.debug { "dnsRequest accepted ${readBytes.encodeHex()}" }
    val dnsPackageReceived = readBytes.toDnsPackage()
    logger.debug { "dnsRequest accepted ${json.encodeToString(dnsPackageReceived)}" }

    socket.close()
    coroutineScope {
        launch {
            addCache(dnsPackageReceived)
        }
    }
    return dnsPackageReceived
}
