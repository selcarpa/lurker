import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*

private val logger = KotlinLogging.logger {}

actual fun dohStart() {
    embeddedServer(CIO, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = false)
}

//@OptIn(ExperimentalForeignApi::class)
actual fun registerShutdownHook(exec: () -> Unit) {
//    atexit(staticCFunction(exec))
    logger.warn{"registerShutdownHook is not supported on native"}
}
