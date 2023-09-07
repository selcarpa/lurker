import io.ktor.network.selector.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.Dispatchers
import lurker.Dns
import lurker.configureRouting
import model.config.Config.Configuration
import model.config.Config.ConfigurationUrl

fun main(args: Array<String>) {
    args.forEach {
        if (it.startsWith("-c=")) {
            ConfigurationUrl = it.replace("-c=", "")
        }
    }

    if (Configuration.doh.enable) {
        embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
            .start(wait = false)
    }

    if (Configuration.dns.udp.enable) {
        Dns.startServer(selectorManager = SelectorManager(Dispatchers.IO), port = Configuration.dns.udp.port)
    }

    print("Blessed are those who mourn, for they shall be comforted.")
}

fun Application.module() {
    configureRouting()
}
