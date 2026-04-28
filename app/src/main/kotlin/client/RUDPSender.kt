package ru.qwuadrixx.app.client

import exception.ServerTimeoutException
import net.packet.RUDPPacket
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
class RUDPSender(
    private val channel: DatagramChannel,
    private val serverAddress: InetSocketAddress,
    private val maxRetries: Int,
    private val timeoutMs: Long
) {

    private val recvBuffer = ByteBuffer.allocate(1500)

    fun ping() {
        repeat(maxRetries) { attempt ->
            channel.send(RUDPPacket.byteBufferPING().flip(), serverAddress)
            if (pollACK()) return
            if (attempt == maxRetries - 1) throw ServerTimeoutException("Сервер не отвечает, попробуйте снова позже")
        }
        throw ServerTimeoutException("Сервер не отвечает, попробуйте снова позже")
    }

    fun sendPackets(packets: List<RUDPPacket>) {
        for (packet in packets) {
            var sent = false
            repeat(maxRetries) { attempt ->
                if (sent) return@repeat
                channel.send(packet.toByteBuffer(), serverAddress)
                if (pollACK()) {
                    sent = true
                } else if (attempt == maxRetries - 1) {
                    throw ServerTimeoutException("Сервер не отвечает, попробуйте снова позже")
                }
            }
        }
    }

    fun sendACK() {
        channel.send(RUDPPacket.byteBufferACK().flip(), serverAddress)
    }

    private fun pollACK(): Boolean {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            recvBuffer.clear()
            val addr = channel.receive(recvBuffer)
            if (addr != null) {
                recvBuffer.flip()
                return recvBuffer.limit() > 0 && RUDPPacket.isByteBufferACK(recvBuffer)
            }
            Thread.sleep(1)
        }
        return false
    }
}
