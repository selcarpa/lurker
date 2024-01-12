package core

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.serialization.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withTimeoutOrNull
import model.config.Config
import model.protocol.DnsPackage.Companion.toByteArray
import model.protocol.DnsPackage.Companion.toDnsPackage
import utils.encodeHex
import kotlin.time.Duration.Companion.seconds

private val logger = KotlinLogging.logger {}

fun Application.configureRouting() {
    routing {
        get("/dns-query") { getDnsQuery() }
        post("/dns-query") { postDnsQuery() }
    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.postDnsQuery() {
    if (call.request.contentType().match("application/dns-message")) {
        val dnsByteArray = call.receive<ByteArray>()
        solveDnsQuery(dnsByteArray)


    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.getDnsQuery() {
    if (call.request.contentType().match("application/dns-message")) {
        call.request.queryParameters["dns"]?.let {
            val dnsByteArray = it.toByteArray()
            solveDnsQuery(dnsByteArray)
        }
    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.solveDnsQuery(dnsByteArray: ByteArray) {
    logger.info { "doh interface receive: ${dnsByteArray.encodeHex()}" }
    val dnsPackage = dnsByteArray.toDnsPackage()
    logger.info { "doh interface receive: $dnsPackage" }

    //TODO: to read cache but not forward the request
    withTimeoutOrNull(Config.Configuration.timeout.seconds) {
        dnsRequest(
            SelectorManager(Dispatchers.IO),
            dnsPackage,
            InetSocketAddress(Config.Configuration.recursive.upstream[0].host, 53)
        )
    }.also { recursiveResult ->
        if (recursiveResult == null) {
            logger.error { "timeout" }
            call.respondBytes(dnsPackage.toByteArray())
        } else {
            call.respondBytes(recursiveResult.toByteArray())
        }
    }
}
