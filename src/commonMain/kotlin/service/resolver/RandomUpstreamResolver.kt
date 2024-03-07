package service.resolver

import kotlin.random.Random

class RandomUpstreamResolver(private val resolvers: List<Resolver>) : Resolver {

    override fun resolve(callback: () -> Unit) {
        resolvers[Random.nextInt(resolvers.size)].resolve(callback)
    }
}
