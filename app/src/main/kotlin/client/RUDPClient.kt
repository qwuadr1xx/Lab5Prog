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
class RUDPClient(private val assembler: IAssembler) {
    private val datagramChannel: DatagramChannel = DatagramChannel.open()

    fun sendAndReceive(request: IRequest): IResponse {
        val listOfPackets = RUDPPacketSplitter(request)
        pingServer()
        sendAndCheck(listOfPackets)
        val response = receiveResponse()
//        finishConnection()
        return response
    }

    private fun pingServer() {
        val byteBuffer = ByteBuffer.allocate(1500)
        for (attempts in 1..MAX_RETRIES) {
            try {
                byteBuffer.clear()
                datagramChannel.write(RUDPPacket.byteBufferPING())
                val bytesRead = datagramChannel.read(byteBuffer.flip())
                if (bytesRead > 0 && RUDPPacket.isByteBufferACK(byteBuffer)) break
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
                    if (bytesRead > 0 && RUDPPacket.isByteBufferACK(byteBuffer)) break
                } catch (e: SocketTimeoutException) {
                    if (attempts == MAX_RETRIES) {
                        throw ServerTimeoutException("Сервер не отвечает, попробуйте снова позже")
                    }
                }
            }
        }
    }

    private fun receiveResponse(): IResponse {
        val byteBuffer = ByteBuffer.allocate(1500)
        while (true) {
            for (attempts in 1..MAX_RETRIES) {
                try {
                    byteBuffer.clear()
                    datagramChannel.read(byteBuffer)
                    val packet = RUDPPacket.fromByteBuffer(byteBuffer.flip())
                    datagramChannel.write(RUDPPacket.byteBufferACK())
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

//    private fun finishConnection() {
//        val byteBuffer = ByteBuffer.allocate(1500)
//        for (attempts in 1..MAX_RETRIES) {
//            try {
//                byteBuffer.clear()
//                datagramChannel.write(RUDPPacket.byteBufferFIN())
//                val bytesRead = datagramChannel.read(byteBuffer)
//                if (bytesRead > 0 && RUDPPacket.isByteBufferFIN(byteBuffer)) break
//            } catch (_: SocketTimeoutException) {
//
//            }
//        }
//    }

    companion object {
        const val SERVER_PORT = 8081
        const val MAX_RETRIES = 3
    }

    init {
        datagramChannel.connect(InetSocketAddress(SERVER_PORT))
        datagramChannel.socket().soTimeout = 3000
    }
}