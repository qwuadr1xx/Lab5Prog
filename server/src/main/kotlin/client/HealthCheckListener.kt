package ru.qwuadrixx.client

import org.slf4j.LoggerFactory
import java.net.DatagramPacket
import java.net.DatagramSocket

class HealthCheckListener(port: Int) : Runnable {

    private val socket = DatagramSocket(port)
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