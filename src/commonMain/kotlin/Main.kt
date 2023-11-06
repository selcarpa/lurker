import io.ktor.network.selector.*
import io.ktor.server.application.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.runBlocking
import lurker.Dns
import lurker.configureRouting
import model.config.Config.Configuration
import model.config.Config.ConfigurationUrl

fun main(args: Array<String>) = runBlocking {
    args.forEach {
        if (it.startsWith("-c=")) {//load the configuration file
            ConfigurationUrl = it.replace("-c=", "")
        }
    }

    if (Configuration.doh.enable) {
        dohStart()
    }

    if (Configuration.dns.udp.enable) {
        Dns.startServer(selectorManager = SelectorManager(Dispatchers.IO), port = Configuration.dns.udp.port)
    }

    print("Blessed are those who mourn, for they shall be comforted.")
}

expect fun dohStart()

fun Application.module() {
    configureRouting()
}
