package ru.qwuadrixx

import org.koin.core.context.startKoin
import ru.qwuadrixx.client.DbChangeListener
import ru.qwuadrixx.client.HealthCheckListener
import ru.qwuadrixx.client.RUDPServer
import ru.qwuadrixx.di.serverModule
import java.sql.Connection

fun main() {
    val koin = startKoin {
        modules(serverModule)
    }.koin

    val healthCheckListener = koin.get<HealthCheckListener>()
    val dbChangeListener = koin.get<DbChangeListener>()
    val rudpServer = koin.get<RUDPServer>()

    healthCheckListener.registerWithBalancer()

    val dbListenerThread = Thread(dbChangeListener, "db-change-listener").also { it.isDaemon = true; it.start() }

    Runtime.getRuntime().addShutdownHook(Thread({
        rudpServer.shutdown()
        healthCheckListener.close()
        dbListenerThread.interrupt()
        koin.get<Connection>().close()
    }, "shutdown-hook"))

    rudpServer.runner()
}