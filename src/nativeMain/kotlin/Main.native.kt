import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.staticCFunction
import platform.posix.atexit

actual fun dohStart() {
    embeddedServer(CIO, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = false)
}

@OptIn(ExperimentalForeignApi::class)
actual fun registerShutdownHook(exec: () -> Unit) {
    atexit(staticCFunction(exec))
}
