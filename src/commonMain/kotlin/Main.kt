import database.model.SystemOperation
import database.model.SystemOperationType
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.network.selector.*
import io.ktor.server.application.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import core.Dns
import core.configureRouting
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import model.config.Config.Configuration
import model.config.Config.ConfigurationUrl

private val logger = KotlinLogging.logger {}

fun main(args: Array<String>) = runBlocking {

    args.forEach {
        if (it.startsWith("-c=")) {//load the configuration file
            ConfigurationUrl = it.replace("-c=", "")
        }
    }

    //detect the configuration if it is loaded
    Configuration

    //insert startup operation
    startupEvent()

    //register shutdown hook
    registerShutdownHook {
        shutdownEvent()
    }

    if (Configuration.doh.enable) {
        dohStart()
    }

    if (Configuration.dns.udp.enable) {
        launch {
            Dns.startServer(selectorManager = SelectorManager(Dispatchers.IO), port = Configuration.dns.udp.port)
        }
    }

    logger.info { "Blessed are those who mourn, for they shall be comforted." }
}

private fun shutdownEvent() {
    SystemOperation.insert(
        SystemOperationType.SHUTDOWN
    )
}

private fun startupEvent() {
    SystemOperation.insert(
        SystemOperationType.STARTUP
    )
}

 fun dohStart(){
     embeddedServer(CIO, port = 8080, host = "0.0.0.0", module = Application::module)
         .start(wait = false)
 }

expect fun registerShutdownHook(exec: () -> Unit)

fun Application.module() {
    configureRouting()
}
