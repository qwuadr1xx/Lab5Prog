package ru.qwuadrixx

import org.koin.core.context.startKoin
import ru.qwuadrixx.client.CollectionSyncListener
import ru.qwuadrixx.client.HealthCheckListener
import ru.qwuadrixx.client.RUDPServer
import ru.qwuadrixx.di.serverModule

fun main() {
    val koin = startKoin {
        modules(serverModule)
    }.koin

    val healthCheckListener = koin.get<HealthCheckListener>()
    Thread(healthCheckListener, "health-check-listener").also { it.isDaemon = true }.start()

    val syncListener = koin.get<CollectionSyncListener>()
    Thread(syncListener, "collection-sync-listener").also { it.isDaemon = true }.start()

    koin.get<RUDPServer>().runner()
}
