package ru.qwuadrixx.balancer

import org.koin.core.context.startKoin
import org.slf4j.LoggerFactory
import ru.qwuadrixx.balancer.di.BalancerConfig
import ru.qwuadrixx.balancer.di.balancerModule
import ru.qwuadrixx.balancer.health.NodeHealthChecker

private val logger = LoggerFactory.getLogger("Main")

fun main() {
    val koin = startKoin {
        modules(balancerModule)
    }.koin

    val config = koin.get<BalancerConfig>()
    if (config.servers.isEmpty()) {
        logger.error("Список серверов пуст — балансировщик не запущен")
        return
    }

    val healthChecker = koin.get<NodeHealthChecker>()
    Thread(healthChecker, "health-checker").also { it.isDaemon = true }.start()

    koin.get<RUDPBalancer>().start()
}