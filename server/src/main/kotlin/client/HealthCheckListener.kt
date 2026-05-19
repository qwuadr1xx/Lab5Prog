@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package ru.qwuadrixx.client

import net.packet.RUDPPacket
import org.slf4j.LoggerFactory
import ru.qwuadrixx.di.ServerConfig
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketTimeoutException

class HealthCheckListener(val socket: DatagramSocket, private val config: ServerConfig) {

    private val logger = LoggerFactory.getLogger(HealthCheckListener::class.java)

    fun registerWithBalancer() {
        val balancerAddr = InetAddress.getByName(config.balancerHost)
        val ping = RUDPPacket.byteArrayBALANCERPING()
        val ackBuffer = DatagramPacket(ByteArray(16), 16)

        repeat(config.maxRetries) { attempt ->
            try {
                socket.send(DatagramPacket(ping, ping.size, balancerAddr, config.balancerPort))
                socket.receive(ackBuffer)
                val bytes = ackBuffer.data.copyOf(ackBuffer.length)
                if (RUDPPacket.isByteArrayACK(bytes)) {
                    logger.info("Сервер зарегистрирован на балансировщике {}:{} (порт {})",
                        config.balancerHost, config.balancerPort, socket.localPort)
                    return
                }
            } catch (_: SocketTimeoutException) {
                logger.warn("Нет ответа от балансировщика (попытка {}/{})", attempt + 1, config.maxRetries)
            }
        }
        logger.error("Не удалось зарегистрироваться на балансировщике {}:{}", config.balancerHost, config.balancerPort)
    }

    fun port(): Int = socket.localPort

    fun close() {
        socket.close()
    }
}