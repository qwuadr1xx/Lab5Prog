package ru.qwuadrixx.balancer.receiver

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
class BalancerReceiver(
    private val socket: DatagramSocket,
    private val assembler: IAssembler,
    private val maxRetries: Int
) {

    fun receivePing(incomingPacket: DatagramPacket): Pair<InetAddress, Int> {
        var timeoutCount = 0
        while (timeoutCount < maxRetries) {
            try {
                socket.receive(incomingPacket)
                val bytes = incomingPacket.data.copyOf(incomingPacket.length)
                if (bytes.isNotEmpty() && RUDPPacket.isByteArrayPING(bytes)) {
                    val ack = RUDPPacket.byteArrayACK()
                    socket.send(DatagramPacket(ack, ack.size, incomingPacket.address, incomingPacket.port))
                    return Pair(incomingPacket.address, incomingPacket.port)
                }
            } catch (_: SocketTimeoutException) {
                timeoutCount++
            }
        }
        throw ServerTimeoutException("Таймаут ожидания PING от клиента")
    }

    fun receiveRequest(clientAddress: InetAddress, clientPort: Int): IRequest {
        val incomingPacket = DatagramPacket(ByteArray(1500), 1500)
        while (true) {
            for (attempt in 1..maxRetries) {
                try {
                    socket.receive(incomingPacket)
                    val packetBytes = incomingPacket.data.copyOf(incomingPacket.length)
                    if (packetBytes.size < RUDPPacket.HEADING) {
                        if (packetBytes.isNotEmpty() && RUDPPacket.isByteArrayPING(packetBytes)) {
                            val ack = RUDPPacket.byteArrayACK()
                            socket.send(DatagramPacket(ack, ack.size, clientAddress, clientPort))
                        }
                        break
                    }
                    val packet = RUDPPacket.fromByteArray(packetBytes)
                    if (packet.length > 0) {
                        val ack = RUDPPacket.byteArrayACK()
                        socket.send(DatagramPacket(ack, ack.size, clientAddress, clientPort))
                        assembler.addPacket(packet)
                        if (assembler.isComplete(packet.uuid)) return requestDeserializer(assembler.assemble(packet.uuid))
                        break
                    }
                } catch (e: SocketTimeoutException) {
                    if (attempt == maxRetries) throw ServerTimeoutException("Таймаут получения запроса от клиента")
                }
            }
        }
    }
}