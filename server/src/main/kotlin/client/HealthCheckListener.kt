package ru.qwuadrixx.client

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.LoggerFactory
import ru.qwuadrixx.di.ServerConfig
import java.net.DatagramPacket
import java.net.DatagramSocket

class HealthCheckListener : KoinComponent, Runnable {

    private val config: ServerConfig by inject()
    private val socket = DatagramSocket(config.port + config.healthPortOffset)
    private val logger = LoggerFactory.getLogger(HealthCheckListener::class.java)

    override fun run() {
        logger.info("HealthCheckListener запущен на порту {}", socket.localPort)
        val buffer = DatagramPacket(ByteArray(16), 16)
        while (!Thread.currentThread().isInterrupted) {
            try {
                socket.receive(buffer)
                val ack = byteArrayOf(1)
                socket.send(DatagramPacket(ack, ack.size, buffer.address, buffer.port))
            } catch (_: InterruptedException) {
                break
            } catch (_: Exception) {}
        }
    }
}
