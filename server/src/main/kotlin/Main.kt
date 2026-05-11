package ru.qwuadrixx

import org.koin.core.context.startKoin
import org.slf4j.LoggerFactory
import ru.qwuadrixx.client.HealthCheckListener
import ru.qwuadrixx.client.RUDPServer
import ru.qwuadrixx.di.serverModule
import kotlin.uuid.ExperimentalUuidApi

private val logger = LoggerFactory.getLogger("Main")

@OptIn(ExperimentalUuidApi::class)
fun main() {
    val koin = startKoin {
        modules(serverModule)
    }.koin

    val healthCheckListener = koin.get<HealthCheckListener>()
    Thread(healthCheckListener, "health-check-listener").also { it.isDaemon = true }.start()

    koin.get<RUDPServer>().runner()
}
