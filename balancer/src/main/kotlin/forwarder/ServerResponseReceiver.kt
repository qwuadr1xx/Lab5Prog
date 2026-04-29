package ru.qwuadrixx.balancer.forwarder

import exception.ServerTimeoutException
import net.assembler.IAssembler
import net.packet.RUDPPacket
import net.responses.IResponse
import utils.responseDeserializer
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketTimeoutException
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
class ServerResponseReceiver(
    private val socket: DatagramSocket,
    private val assembler: IAssembler,
    private val maxRetries: Int
) {
    private val buffer = DatagramPacket(ByteArray(1500), 1500)

    fun receiveResponse(serverAddress: InetAddress, serverPort: Int): IResponse {
        while (true) {
            for (attempt in 1..maxRetries) {
                try {
                    socket.receive(buffer)
                    val bytes = buffer.data.copyOf(buffer.length)
                    if (bytes.size < RUDPPacket.HEADING) break
                    val packet = RUDPPacket.fromByteArray(bytes)
                    if (packet.length > 0) {
                        val ack = RUDPPacket.byteArrayACK()
                        socket.send(DatagramPacket(ack, ack.size, serverAddress, serverPort))
                        assembler.addPacket(packet)
                        if (assembler.isComplete(packet.uuid)) return responseDeserializer(assembler.assemble(packet.uuid))
                        break
                    }
                } catch (e: SocketTimeoutException) {
                    if (attempt == maxRetries) throw ServerTimeoutException("Таймаут получения ответа от сервера")
                }
            }
        }
    }
}