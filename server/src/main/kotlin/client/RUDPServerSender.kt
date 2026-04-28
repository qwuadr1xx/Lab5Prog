package ru.qwuadrixx.client

import exception.ServerTimeoutException
import net.packet.RUDPPacket
import org.slf4j.LoggerFactory
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketTimeoutException
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
class RUDPServerSender(
    private val socket: DatagramSocket,
    private val maxRetries: Int
) {
    private val logger = LoggerFactory.getLogger(RUDPServerSender::class.java)

    fun sendResponse(responsePackets: List<RUDPPacket>, clientAddress: InetAddress, clientPort: Int) {
        val ackBuffer = DatagramPacket(ByteArray(1500), 1500)
        for (packet in responsePackets) {
            for (attempt in 1..maxRetries) {
                val packetBytes = packet.toByteArray()
                try {
                    socket.send(DatagramPacket(packetBytes, packetBytes.size, clientAddress, clientPort))
                    socket.receive(ackBuffer)
                    if (ackBuffer.length > 0 && RUDPPacket.isByteArrayACK(ackBuffer.data.copyOf(ackBuffer.length))) break
                } catch (e: SocketTimeoutException) {
                    if (attempt == maxRetries) {
                        logger.warn("Не удалось получить ACK от клиента после {} попыток", maxRetries)
                        throw ServerTimeoutException("Таймаут ожидания ACK от клиента")
                    }
                }
            }
        }
    }
}