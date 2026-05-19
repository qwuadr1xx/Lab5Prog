package ru.qwuadrixx.balancer

import org.koin.core.context.startKoin
import ru.qwuadrixx.balancer.auth.TokenCacheEvictor
import ru.qwuadrixx.balancer.di.balancerModule
import ru.qwuadrixx.balancer.health.NodeHealthChecker

fun main() {
    val koin = startKoin {
        modules(balancerModule)
    }.koin

    Thread(koin.get<NodeHealthChecker>(), "health-checker").also { it.isDaemon = true }.start()
    Thread(koin.get<TokenCacheEvictor>(), "token-cache-evictor").also { it.isDaemon = true }.start()

    koin.get<RUDPBalancer>().start()
}