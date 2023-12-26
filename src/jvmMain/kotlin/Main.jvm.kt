actual fun registerShutdownHook(exec: () -> Unit) {
    Runtime.getRuntime().addShutdownHook(Thread({ exec() }, "bye"))
}
