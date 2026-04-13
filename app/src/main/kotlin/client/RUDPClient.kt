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
class RUDPClient(private var datagramChannel: DatagramChannel, private val assembler: IAssembler) {
    fun sendAndReceive(request: IRequest): IResponse {
        val listOfPackets = RUDPPacketSplitter(request)
        pingServer()
        sendAndCheck(listOfPackets)
        val response = receiveAnswer()
        finishConnection()
        return response
    }

    private fun pingServer() {
        val byteBuffer = ByteBuffer.allocate(1500)
        for (attempts in 1..MAX_RETRIES) {
            try {
                byteBuffer.clear()
                datagramChannel.write(RUDPPacket.PING())
                val bytesRead = datagramChannel.read(byteBuffer.flip())
                if (bytesRead > 0 && RUDPPacket.isACK(byteBuffer)) break
            } catch (e: SocketTimeoutException) {
                if (attempts == MAX_RETRIES) {
                    throw ServerTimeoutException("Сервер не отвечает, попробуйте снова позже")
                }
            }
        }
    }

    private fun sendAndCheck(listOfPackets: List<RUDPPacket>) {
        val byteBuffer = ByteBuffer.allocate(1500)
        for (packet in listOfPackets) {
            for (attempts in 1..MAX_RETRIES) {
                try {
                    byteBuffer.clear()
                    datagramChannel.write(packet.toByteBuffer())
                    val bytesRead = datagramChannel.read(byteBuffer.flip())
                    if (bytesRead > 0 && RUDPPacket.isACK(byteBuffer)) break
                } catch (e: SocketTimeoutException) {
                    if (attempts == MAX_RETRIES) {
                        throw ServerTimeoutException("Сервер не отвечает, попробуйте снова позже")
                    }
                }
            }
        }
    }

    private fun receiveAnswer(): IResponse {
        val byteBuffer = ByteBuffer.allocate(1500)
        while (true) {
            for (attempts in 1..MAX_RETRIES) {
                try {
                    byteBuffer.clear()
                    datagramChannel.read(byteBuffer)
                    val packet = RUDPPacket.fromByteBuffer(byteBuffer.flip())
                    datagramChannel.write(RUDPPacket.ACK())
                    assembler.addPacket(packet)
                    if (assembler.isComplete(packet.uuid)) return responseDeserializer(assembler.assemble(packet.uuid))
                    break
                } catch (e: SocketTimeoutException) {
                    if (attempts == MAX_RETRIES) {
                        throw ServerTimeoutException("Сервер не отвечает, попробуйте снова позже")
                    }
                }
            }
        }
    }

    private fun finishConnection() {
        val byteBuffer = ByteBuffer.allocate(1500)
        for (attempts in 1..MAX_RETRIES) {
            try {
                byteBuffer.clear()
                datagramChannel.write(RUDPPacket.FIN())
                val bytesRead = datagramChannel.read(byteBuffer)
                if (bytesRead > 0 && RUDPPacket.isFIN(byteBuffer)) break
            } catch (e: SocketTimeoutException) {
                if (attempts == MAX_RETRIES) {
                    throw ServerTimeoutException("Сервер не отвечает, попробуйте снова позже")
                }
            }
        }
    }


    companion object {
        const val SERVER_PORT = 8081
        const val MAX_RETRIES = 3
    }

    init {
        datagramChannel = DatagramChannel.open()
        datagramChannel.connect(InetSocketAddress(SERVER_PORT))
        datagramChannel.socket().soTimeout = 3000
    }
}