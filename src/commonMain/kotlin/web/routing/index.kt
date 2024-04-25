package web.routing

import exit
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import model.config.Config.Configuration
import model.protocol.DnsPackage.Companion.toDnsPackage
import utils.json

private val logger = KotlinLogging.logger {}
fun Application.configureWebRouting() {
    install(ContentNegotiation) {
        Json {
            json
        }
    }

    installLog()


    logger.info { "website start with port: ${Configuration.web.port}" }

    routing {
        get("/") {
            call.respondRedirect("https://example.tain.one")
        }
        // Exit the application
        get("/api/exit") {
            logger.warn { "exit the application" }
            call.respond("success")
            exit()
        }
        get("/api/decode") {
            call.request.queryParameters["data"]?.let {
                call.respond(it.decodeBase64Bytes().toDnsPackage())
            }
        }
        post("/api/decode") {
            call.receive<DecodeReq>().let {
                call.respond(it.data.decodeBase64Bytes().toDnsPackage())
            }
        }
    }
}

@Serializable
data class DecodeReq(val data: String)

expect fun Application.installLog()
