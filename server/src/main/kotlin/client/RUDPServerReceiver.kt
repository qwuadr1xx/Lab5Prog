@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package ru.qwuadrixx.client

import exception.ServerTimeoutException
import net.assembler.Assembler
import net.packet.RUDPPacket
import net.requests.IRequest
import utils.requestDeserializer
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

class RUDPServerReceiver(
    private val socket: DatagramSocket,
    private val maxRetries: Int,
    private val socketTimeoutMs: Int
) {

    fun receiveRequest(queue: LinkedBlockingQueue<ByteArray>, addr: InetAddress, port: Int): IRequest {
        val assembler = Assembler()
        while (true) {
            val bytes = queue.poll(socketTimeoutMs.toLong(), TimeUnit.MILLISECONDS)
                ?: throw ServerTimeoutException("Таймаут получения запроса от $addr:$port")
            if (bytes.size < RUDPPacket.HEADING) continue
            val packet = RUDPPacket.fromByteArray(bytes)
            if (packet.length > 0) {
                send(RUDPPacket.byteArrayACK(), addr, port)
                assembler.addPacket(packet)
                if (assembler.isComplete(packet.uuid)) {
                    return requestDeserializer(assembler.assemble(packet.uuid))
                }
            }
        }
    }

    private fun send(data: ByteArray, address: InetAddress, port: Int) {
        socket.send(DatagramPacket(data, data.size, address, port))
    }
}