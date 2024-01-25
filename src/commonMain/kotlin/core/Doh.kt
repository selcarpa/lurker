package core

import database.model.QueryRecord
import database.model.toQueryRecord
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import io.ktor.util.pipeline.*
import kotlinx.coroutines.*
import kotlinx.serialization.encodeToString
import model.config.Config
import model.protocol.DnsPackage.Companion.toByteArray
import model.protocol.DnsPackage.Companion.toDnsPackage
import utils.encodeHex
import utils.json
import kotlin.time.Duration.Companion.seconds

private val logger = KotlinLogging.logger {}

fun Application.configureDohRouting() {
    routing {
        get("/dns-query") { getDnsQuery() }
        post("/dns-query") { postDnsQuery() }
    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.postDnsQuery() {
    if (call.request.contentType().match("application/dns-message")) {
        val dnsByteArray = call.receive<ByteArray>()
        solveDnsQuery(dnsByteArray, call.toRemoteAddr())
    } else {
        call.respondRedirect("/")
    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.getDnsQuery() {
    if (call.request.contentType().match("application/dns-message")) {
        call.request.queryParameters["dns"]?.let {
            val dnsByteArray = it.decodeBase64Bytes()
            solveDnsQuery(dnsByteArray, call.toRemoteAddr())
        }
    } else {
        call.respondRedirect("/")
    }
}

private fun ApplicationCall.toRemoteAddr(): String = if (request.header("X-Forwarded-For") != null) {
    this.request.header("X-Forwarded-For")!!
} else {
    request.origin.remoteAddress
}

private suspend fun PipelineContext<Unit, ApplicationCall>.solveDnsQuery(dnsByteArray: ByteArray, queryFrom: String) {
    logger.debug { "doh interface receive: ${dnsByteArray.encodeHex()}" }
    val dnsPackage = dnsByteArray.toDnsPackage()
    logger.debug { "doh interface receive: ${json.encodeToString(dnsPackage)}" }

    coroutineScope {
        launch {
            QueryRecord.insertBatch(dnsPackage.questions.map {
                it.toQueryRecord(queryFrom)
            })
        }
    }

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
