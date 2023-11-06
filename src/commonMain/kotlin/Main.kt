import database.model.SystemOperation
import database.model.SystemOperationType
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.network.selector.*
import io.ktor.server.application.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import lurker.Dns
import lurker.configureRouting
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

expect fun dohStart()

expect fun registerShutdownHook(exec: () -> Unit)

fun Application.module() {
    configureRouting()
}
