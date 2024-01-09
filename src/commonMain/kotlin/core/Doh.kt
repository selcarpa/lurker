package core

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import model.config.Config
import model.protocol.DnsPackage.Companion.toByteArray
import model.protocol.DnsPackage.Companion.toDnsPackage
import service.resolve
import kotlin.time.Duration.Companion.seconds

private val logger = KotlinLogging.logger {}

fun Application.configureRouting() {
    routing {
        get("/dns-query") {
            if (call.request.contentType().match("application/dns-message")) {
                call.request.queryParameters.get("dns")?.let {
                    val dnsPackage = it.toByteArray().toDnsPackage()

                    //TODO: to read cache but not forward the request
                    withTimeoutOrNull(Config.Configuration.timeout.seconds) {
                        dnsRequest(SelectorManager(Dispatchers.IO), dnsPackage, InetSocketAddress("8.8.8.8", 53))
                    }.also { recursiveResult ->
                        if (recursiveResult == null) {
                            logger.error { "timeout" }
                            call.respondBytes(dnsPackage.toByteArray())
                        } else {
                            call.respondBytes(recursiveResult.toByteArray())
                        }
                    }
                }
            }
        }
        post("/dns-query") {
            if (call.request.contentType().match("application/dns-message")) {
                val dnsPackage = call.request
            }
        }
    }
}
