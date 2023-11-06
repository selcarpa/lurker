import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

actual fun dohStart() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module).start(wait = false)
}

actual fun registerShutdownHook(exec: () -> Unit) {
    Runtime.getRuntime().addShutdownHook(Thread({ exec() }, "bye"))
}
