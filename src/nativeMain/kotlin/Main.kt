
import io.ktor.network.selector.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import lurker.Dns
import lurker.configureRouting
import model.config.Config.ConfigurationUrl

fun main(args: Array<String>) {
    args.forEach {
        if (it.startsWith("-c=")) {
            ConfigurationUrl = it.replace("-c=", "")
        }
    }

    embeddedServer(CIO, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = false)

    Dns.startServer(selectorManager = SelectorManager(Dispatchers.IO), port = 18888)
//    Dns.sendADnsRequest(selectorManager = SelectorManager(Dispatchers.IO))


    print("Blessed are those who mourn, for they shall be comforted.")
}

fun Application.module() {
    configureRouting()
}
