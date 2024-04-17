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
import core.configureDohRouting
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import model.config.Config.Configuration
import model.config.Config.ConfigurationUrl
import web.routing.configureWebRouting

private val logger = KotlinLogging.logger {}


fun main(args: Array<String>) = runBlocking {

    args.forEach {
        if (it.startsWith("-c=")) {//load the configuration file
            ConfigurationUrl = it.replace("-c=", "")
        }
    }
    //set log appender
    logAppenderSet()

    //detect the configuration if it is loaded and solve debug task
    if (Configuration.debug) {
        debugLogSet()
    }
    logger.debug { "debug enabled" }

    //insert startup operation
    startupEvent()

    //register shutdown hook
    registerShutdownHook {
        shutdownEvent()
    }

    //anyway to start the dns server
    if (Configuration.doh.enable) {
        dohStart()
    }

    //and start the dns server
    if (Configuration.dns.udp.enable) {
        launch {
            Dns.startServer(selectorManager = SelectorManager(Dispatchers.IO), port = Configuration.dns.udp.port)
        }
    }

    //and start the tcp dns server
    if (Configuration.dns.tcp.enable) {
        launch {
            Dns.startTcpServer(selectorManager = SelectorManager(Dispatchers.IO), port = Configuration.dns.tcp.port)
        }
    }

    //if website enable and not listen the same port as doh, then configure web routing, if website listen the same port as doh, then configure web routing in doh start process
    if (Configuration.web.enable && Configuration.web.port != Configuration.doh.port) {
        webStart()
    }

    logger.info { "Blessed are those who mourn, for they shall be comforted." }
}

fun webStart() {
    embeddedServer(CIO, port = Configuration.web.port, host = "0.0.0.0", module = {
        configureWebRouting()
    }).start(wait = false)
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

fun dohStart() {
    embeddedServer(CIO, port = Configuration.doh.port, host = "0.0.0.0", module = {
        configureDohRouting()
        //if website listen the same port as doh, then configure web routing
        if (Configuration.web.enable && Configuration.web.port == Configuration.doh.port) {
            configureWebRouting()
        }
    }).start(wait = false)
}

expect fun registerShutdownHook(exec: () -> Unit)
expect fun logAppenderSet()
expect fun debugLogSet()
expect fun exit()
