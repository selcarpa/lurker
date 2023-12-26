import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

//@OptIn(ExperimentalForeignApi::class)
actual fun registerShutdownHook(exec: () -> Unit) {
//    atexit(staticCFunction(exec))
    logger.warn { "registerShutdownHook is not supported on native" }
}
