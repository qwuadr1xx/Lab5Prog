@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package ru.qwuadrixx.client

import exception.ServerTimeoutException
import net.packet.RUDPPacket
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

class RUDPServerSender(
    private val socket: DatagramSocket,
    private val maxRetries: Int,
    private val socketTimeoutMs: Int
) {

    fun sendResponse(packets: List<RUDPPacket>, queue: LinkedBlockingQueue<ByteArray>, addr: InetAddress, port: Int) {
        for (packet in packets) {
            var acked = false
            repeat(maxRetries) {
                if (!acked) {
                    send(packet.toByteArray(), addr, port)
                    val ackBytes = queue.poll(socketTimeoutMs.toLong(), TimeUnit.MILLISECONDS)
                    if (ackBytes != null && RUDPPacket.isByteArrayACK(ackBytes)) {
                        acked = true
                    }
                }
            }
            if (!acked) throw ServerTimeoutException("Нет ACK от клиента $addr:$port")
        }
    }

    private fun send(data: ByteArray, address: InetAddress, port: Int) {
        socket.send(DatagramPacket(data, data.size, address, port))
    }
}