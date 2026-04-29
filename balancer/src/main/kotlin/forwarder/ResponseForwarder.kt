package ru.qwuadrixx.balancer.forwarder

import exception.ServerTimeoutException
import net.packet.RUDPPacket
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketTimeoutException
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
class ResponseForwarder(
    private val socket: DatagramSocket,
    private val maxRetries: Int
) {
    private val ackBuffer = DatagramPacket(ByteArray(1500), 1500)

    fun sendResponse(packets: List<RUDPPacket>, clientAddress: InetAddress, clientPort: Int) {
        for (packet in packets) {
            for (attempt in 1..maxRetries) {
                val packetBytes = packet.toByteArray()
                try {
                    socket.send(DatagramPacket(packetBytes, packetBytes.size, clientAddress, clientPort))
                    socket.receive(ackBuffer)
                    if (ackBuffer.length > 0 && RUDPPacket.isByteArrayACK(ackBuffer.data.copyOf(ackBuffer.length))) break
                } catch (e: SocketTimeoutException) {
                    if (attempt == maxRetries) throw ServerTimeoutException("Таймаут возврата ответа клиенту")
                }
            }
        }
    }
}