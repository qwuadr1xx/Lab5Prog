package ru.qwuadrixx.client

import exception.ServerTimeoutException
import net.assembler.IAssembler
import net.packet.RUDPPacket
import net.requests.IRequest
import org.slf4j.LoggerFactory
import ru.qwuadrixx.managers.RequestManager
import utils.requestDeserializer
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketTimeoutException
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
class RUDPServer(
    private val assembler: IAssembler,
    private val requestManager: RequestManager,
    private val port: Int,
    private val maxRetries: Int,
    private val socketTimeoutMs: Int
) {
    private val datagramSocket: DatagramSocket = DatagramSocket(port).apply {
        soTimeout = socketTimeoutMs
    }
    private val logger = LoggerFactory.getLogger(RUDPServer::class.java)

    fun runner() {
        logger.info("Сервер запущен на порту {}", port)
        while (true) {
            try {
                val incomingPacket = DatagramPacket(ByteArray(1500), 1500)
                val (clientAddress, clientPort) = receivePing(incomingPacket)
                logger.debug("Получено PING от {}:{}", clientAddress, clientPort)
                val request = receiveRequest(clientAddress, clientPort)
                logger.info("Получен запрос: {}", request.commandName)
                val response = requestManager.dispatch(request)
                val responsePackets = utils.RUDPPacketSplitter(response)
                sendResponse(responsePackets, clientAddress, clientPort)
                logger.debug("Ответ отправлен клиенту {}:{}", clientAddress, clientPort)
            } catch (_: ServerTimeoutException) {
                logger.debug("Таймаут соединения, ожидание следующего клиента")
            } catch (e: Exception) {
                logger.error("Ошибка в цикле сервера: {}", e.message, e)
            }
        }
    }

    private fun receivePing(incomingPacket: DatagramPacket): Pair<InetAddress, Int> {
        var timeoutCount = 0
        while (timeoutCount < maxRetries) {
            try {
                datagramSocket.receive(incomingPacket)
                val bytes = incomingPacket.data.copyOf(incomingPacket.length)
                if (bytes.isNotEmpty() && RUDPPacket.isByteArrayPING(bytes)) {
                    val ack = RUDPPacket.byteArrayACK()
                    datagramSocket.send(DatagramPacket(ack, ack.size, incomingPacket.address, incomingPacket.port))
                    return Pair(incomingPacket.address, incomingPacket.port)
                }
            } catch (e: SocketTimeoutException) {
                timeoutCount++
            }
        }
        throw ServerTimeoutException("Таймаут ожидания PING")
    }

    private fun receiveRequest(clientAddress: InetAddress, clientPort: Int): IRequest {
        val incomingPacket = DatagramPacket(ByteArray(1500), 1500)
        while (true) {
            for (attempt in 1..maxRetries) {
                try {
                    datagramSocket.receive(incomingPacket)
                    val packetBytes = incomingPacket.data.copyOf(incomingPacket.length)
                    if (packetBytes.size < RUDPPacket.HEADING) {
                        if (packetBytes.isNotEmpty() && RUDPPacket.isByteArrayPING(packetBytes)) {
                            val ack = RUDPPacket.byteArrayACK()
                            datagramSocket.send(DatagramPacket(ack, ack.size, clientAddress, clientPort))
                        }
                        break
                    }
                    val packet = RUDPPacket.fromByteArray(packetBytes)
                    if (packet.length > 0) {
                        val ack = RUDPPacket.byteArrayACK()
                        datagramSocket.send(DatagramPacket(ack, ack.size, clientAddress, clientPort))
                        assembler.addPacket(packet)
                        if (assembler.isComplete(packet.uuid)) return requestDeserializer(assembler.assemble(packet.uuid))
                        break
                    }
                } catch (e: SocketTimeoutException) {
                    if (attempt == maxRetries) throw ServerTimeoutException("Таймаут получения запроса")
                }
            }
        }
    }

    private fun sendResponse(responsePackets: List<RUDPPacket>, clientAddress: InetAddress, clientPort: Int) {
        val ackBuffer = DatagramPacket(ByteArray(1500), 1500)
        for (packet in responsePackets) {
            for (attempt in 1..maxRetries) {
                val packetBytes = packet.toByteArray()
                try {
                    datagramSocket.send(DatagramPacket(packetBytes, packetBytes.size, clientAddress, clientPort))
                    datagramSocket.receive(ackBuffer)
                    if (ackBuffer.length > 0 && RUDPPacket.isByteArrayACK(ackBuffer.data.copyOf(ackBuffer.length))) break
                } catch (e: SocketTimeoutException) {
                    if (attempt == maxRetries) {
                        logger.warn("Не удалось получить ACK от клиента после {} попыток", maxRetries)
                        throw ServerTimeoutException("Таймаут ожидания ACK от клиента")
                    }
                }
            }
        }
    }
}
