import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*

actual fun dohStart() {
    embeddedServer(CIO, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = false)
}
