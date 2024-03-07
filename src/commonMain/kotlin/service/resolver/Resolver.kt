package service.resolver

interface Resolver {
    fun resolve(callback: () -> Unit)
}
