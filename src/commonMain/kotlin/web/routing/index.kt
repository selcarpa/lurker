package web.routing


import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import model.config.Config.Configuration

private val logger = KotlinLogging.logger {}
fun Application.configureWebRouting() {
    logger.info { "website start with port: ${Configuration.web.port}" }

    routing {
        get("/") {
            call.respondRedirect("https://example.tain.one")
        }
    }
}
