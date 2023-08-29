
import io.ktor.network.selector.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import lurker.Dns
import lurker.configureRouting
fun main() {
    embeddedServer(CIO, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = false)

    Dns.startServer(selectorManager = SelectorManager(Dispatchers.IO), port = 18888)
//    Dns.sendADnsRequest(selectorManager = SelectorManager(Dispatchers.IO))
}

fun Application.module() {
    configureRouting()
}
