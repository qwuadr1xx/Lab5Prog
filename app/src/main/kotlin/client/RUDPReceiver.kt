package ru.qwuadrixx.app.client

import exception.ServerTimeoutException
import net.assembler.IAssembler
import net.packet.RUDPPacket
import net.responses.IResponse
import utils.responseDeserializer
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
class RUDPReceiver(
    private val channel: DatagramChannel,
    private val assembler: IAssembler,
    private val sender: RUDPSender,
    private val maxRetries: Int,
    private val timeoutMs: Long
) {

    private val recvBuffer = ByteBuffer.allocate(1500)

    fun receiveResponse(): IResponse {
        while (true) {
            val packet = pollPacket()
            sender.sendACK()
            assembler.addPacket(packet)
            if (assembler.isComplete(packet.uuid)) {
                return responseDeserializer(assembler.assemble(packet.uuid))
            }
        }
    }

    private fun pollPacket(): RUDPPacket {
        repeat(maxRetries) { attempt ->
            val deadline = System.currentTimeMillis() + timeoutMs
            while (System.currentTimeMillis() < deadline) {
                recvBuffer.clear()
                val addr = channel.receive(recvBuffer)
                if (addr != null) {
                    recvBuffer.flip()
                    if (recvBuffer.limit() >= RUDPPacket.HEADING) {
                        return RUDPPacket.fromByteBuffer(recvBuffer)
                    }
                }
                Thread.sleep(1)
            }
            if (attempt == maxRetries - 1) throw ServerTimeoutException("Сервер не отвечает, попробуйте снова позже")
        }
        throw ServerTimeoutException("Сервер не отвечает, попробуйте снова позже")
    }
}