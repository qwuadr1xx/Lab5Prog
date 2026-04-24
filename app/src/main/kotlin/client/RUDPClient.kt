package ru.qwuadrixx.app.client

import exception.ServerTimeoutException
import net.assembler.IAssembler
import net.packet.RUDPPacket
import net.requests.IRequest
import net.responses.IResponse
import utils.RUDPPacketSplitter
import utils.responseDeserializer
import java.net.InetSocketAddress
import java.net.SocketTimeoutException
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
class RUDPClient(
    private val assembler: IAssembler,
    serverHost: String,
    serverPort: Int,
    private val maxRetries: Int,
    socketTimeoutMs: Int
) : IRUDPClient {

    private val serverAddress = InetSocketAddress(serverHost, serverPort)
    private val datagramChannel: DatagramChannel = DatagramChannel.open().apply {
        socket().soTimeout = socketTimeoutMs
    }

    override fun sendAndReceive(request: IRequest): IResponse {
        val listOfPackets = RUDPPacketSplitter(request)
        pingServer()
        sendAndCheck(listOfPackets)
        return receiveResponse()
    }

    private fun pingServer() {
        val recvBuffer = ByteBuffer.allocate(1500)
        for (attempt in 1..maxRetries) {
            try {
                datagramChannel.send(RUDPPacket.byteBufferPING().flip(), serverAddress)
                recvBuffer.clear()
                datagramChannel.receive(recvBuffer)
                recvBuffer.flip()
                if (recvBuffer.limit() > 0 && RUDPPacket.isByteBufferACK(recvBuffer)) {
                    return
                }
            } catch (e: SocketTimeoutException) {
                if (attempt == maxRetries) throw ServerTimeoutException("Сервер не отвечает, попробуйте снова позже")
            }
        }
        throw ServerTimeoutException("Сервер не отвечает, попробуйте снова позже")
    }

    private fun sendAndCheck(listOfPackets: List<RUDPPacket>) {
        val recvBuffer = ByteBuffer.allocate(1500)
        for ((index, packet) in listOfPackets.withIndex()) {
            for (attempt in 1..maxRetries) {
                try {
                    datagramChannel.send(packet.toByteBuffer(), serverAddress)
                    recvBuffer.clear()
                    datagramChannel.receive(recvBuffer)
                    recvBuffer.flip()
                    if (recvBuffer.limit() > 0 && RUDPPacket.isByteBufferACK(recvBuffer)) {
                        break
                    }
                } catch (e: SocketTimeoutException) {
                    if (attempt == maxRetries) throw ServerTimeoutException("Сервер не отвечает, попробуйте снова позже")
                }
            }
        }
    }

    private fun receiveResponse(): IResponse {
        val recvBuffer = ByteBuffer.allocate(1500)
        while (true) {
            for (attempt in 1..maxRetries) {
                try {
                    recvBuffer.clear()
                    datagramChannel.receive(recvBuffer)
                    recvBuffer.flip()
                    val packet = RUDPPacket.fromByteBuffer(recvBuffer)
                    datagramChannel.send(RUDPPacket.byteBufferACK().flip(), serverAddress)
                    assembler.addPacket(packet)
                    if (assembler.isComplete(packet.uuid)) {
                        return responseDeserializer(assembler.assemble(packet.uuid))
                    }
                    break
                } catch (e: SocketTimeoutException) {
                    if (attempt == maxRetries) throw ServerTimeoutException("Сервер не отвечает, попробуйте снова позже")
                }
            }
        }
    }
}