package web.routing

import io.ktor.server.application.*
import io.ktor.server.plugins.callloging.*
import org.slf4j.event.Level

actual fun Application.installLog() {
    install(CallLogging) {
        level = Level.DEBUG
    }
}
