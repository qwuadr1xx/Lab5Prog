package ru.qwuadrixx.client

import exception.ServerTimeoutException
import net.assembler.IAssembler
import net.packet.RUDPPacket
import net.requests.IRequest
import utils.requestDeserializer
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketTimeoutException
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
class RUDPServer(private val assembler: IAssembler) {
    private val datagramSocket: DatagramSocket = DatagramSocket(SERVER_PORT)

    fun runner() {
        while (true) {
            try {
                val incomingPacket = DatagramPacket(ByteArray(1500), 1500)
                val (address, port) = receivePing(incomingPacket)
                val request = receiveRequest(address, port)
                sendResponse(emptyList(), address, port) // TODO: заменить на реальный список пакетов
            } catch (_: ServerTimeoutException) {

            }
        }
    }

    private fun receivePing(incomingPacket: DatagramPacket): Pair<InetAddress, Int> {
        for (attempts in 1..MAX_RETRIES) {
            try {
                datagramSocket.receive(incomingPacket)
                if (incomingPacket.length > 0 && RUDPPacket.isByteArrayPING(incomingPacket.data.copyOf(incomingPacket.length))) {
                    val ack = RUDPPacket.byteArrayACK()
                    datagramSocket.send(DatagramPacket(ack, ack.size, incomingPacket.address, incomingPacket.port))
                    return Pair(incomingPacket.address, incomingPacket.port)
                } else {
                    throw SocketTimeoutException()
                }
            } catch (e: SocketTimeoutException) {
                if (attempts == MAX_RETRIES) throw ServerTimeoutException("Сервер не отвечает")
            }
        }
        throw ServerTimeoutException("Сервер не отвечает")
    }

    private fun receiveRequest(address: InetAddress, port: Int): IRequest {
        val incomingPacket = DatagramPacket(ByteArray(1500), 1500)
        while (true) {
            for (attempts in 1..MAX_RETRIES) {
                try {
                    datagramSocket.receive(incomingPacket)
                    val byteArray = incomingPacket.data.copyOf(incomingPacket.length)
                    val packet = RUDPPacket.fromByteArray(byteArray)
                    if (packet.length > 0) {
                        val ack = RUDPPacket.byteArrayACK()
                        datagramSocket.send(
                            DatagramPacket(
                                ack,
                                ack.size,
                                address,
                                port
                            )
                        )
                        assembler.addPacket(packet)
                        if (assembler.isComplete(packet.uuid)) return requestDeserializer(assembler.assemble(packet.uuid))
                        break
                    }
                } catch (e: SocketTimeoutException) {
                    if (attempts == MAX_RETRIES) {
                        throw ServerTimeoutException("Сервер не отвечает, попробуйте снова позже")
                    }
                }
            }
        }
    }

    private fun sendResponse(listOfPackets: List<RUDPPacket>, address: InetAddress, port: Int) {
        val incomingPacket = DatagramPacket(ByteArray(1500), 1500)
        for (packet in listOfPackets) {
            for (attempts in 1..MAX_RETRIES) {
                val byteArray = packet.toByteArray()
                try {
                    datagramSocket.send(
                        DatagramPacket(
                            byteArray,
                            byteArray.size,
                            address,
                            port
                        )
                    )
                    datagramSocket.receive(incomingPacket)
                    if (incomingPacket.length > 0 && RUDPPacket.isByteArrayACK(incomingPacket.data.copyOf(incomingPacket.length))) break
                } catch (e: SocketTimeoutException) {
                    if (attempts == MAX_RETRIES) {
                        throw ServerTimeoutException("Сервер не отвечает, попробуйте снова позже")
                    }
                }
            }
        }
    }

//    private fun finishConnection(address: InetAddress, port: Int) {
//        val incomingPacket = DatagramPacket(ByteArray(1500), 1500)
//        for (attempts in 1..MAX_RETRIES) {
//            try {
//                datagramSocket.receive(incomingPacket)
//                if (incomingPacket.length > 0 && RUDPPacket.isByteArrayFIN(incomingPacket.data.copyOf(incomingPacket.length))) {
//                    val ack = RUDPPacket.byteArrayACK()
//                    datagramSocket.send(
//                        DatagramPacket(
//                            ack,
//                            ack.size,
//                            incomingPacket.address,
//                            incomingPacket.port
//                        )
//                    )
//                    return
//                }
//            } catch (_: SocketTimeoutException) {
//
//            }
//        }
//    }


    companion object {
        const val SERVER_PORT = 8081
        const val MAX_RETRIES = 3
    }
}