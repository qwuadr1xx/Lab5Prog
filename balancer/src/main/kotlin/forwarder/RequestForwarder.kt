package ru.qwuadrixx.balancer.forwarder

import exception.ServerTimeoutException
import net.packet.RUDPPacket
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketTimeoutException
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
class RequestForwarder(
    private val socket: DatagramSocket,
    private val maxRetries: Int,
    private val pingTimeoutMs: Int
) {
    private val ackBuffer = DatagramPacket(ByteArray(1500), 1500)

    fun sendPing(serverAddress: InetAddress, serverPort: Int) {
        val prevTimeout = socket.soTimeout
        socket.soTimeout = pingTimeoutMs
        try {
            val ping = RUDPPacket.byteArrayPING()
            socket.send(DatagramPacket(ping, ping.size, serverAddress, serverPort))
            socket.receive(ackBuffer)
            if (!RUDPPacket.isByteArrayACK(ackBuffer.data.copyOf(ackBuffer.length))) {
                throw ServerTimeoutException("Некорректный ACK от серверного узла")
            }
        } catch (_: SocketTimeoutException) {
            throw ServerTimeoutException("Таймаут PING к серверному узлу")
        } finally {
            socket.soTimeout = prevTimeout
        }
    }

    fun sendRequest(packets: List<RUDPPacket>, serverAddress: InetAddress, serverPort: Int) {
        for (packet in packets) {
            for (attempt in 1..maxRetries) {
                val packetBytes = packet.toByteArray()
                try {
                    socket.send(DatagramPacket(packetBytes, packetBytes.size, serverAddress, serverPort))
                    socket.receive(ackBuffer)
                    if (ackBuffer.length > 0 && RUDPPacket.isByteArrayACK(ackBuffer.data.copyOf(ackBuffer.length))) break
                    if (attempt == maxRetries) throw ServerTimeoutException("Таймаут пересылки запроса на сервер")
                } catch (e: SocketTimeoutException) {
                    if (attempt == maxRetries) throw ServerTimeoutException("Таймаут пересылки запроса на сервер")
                }
            }
        }
    }
}