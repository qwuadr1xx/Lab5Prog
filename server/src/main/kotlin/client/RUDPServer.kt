package ru.qwuadrixx.client

import exception.ServerTimeoutException
import net.assembler.IAssembler
import org.slf4j.LoggerFactory
import ru.qwuadrixx.managers.RequestManager
import java.net.DatagramPacket
import java.net.DatagramSocket
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
    private val receiver = RUDPServerReceiver(datagramSocket, assembler, maxRetries)
    private val sender = RUDPServerSender(datagramSocket, maxRetries)

    fun runner() {
        logger.info("Сервер запущен на порту {}", port)
        while (true) {
            try {
                val incomingPacket = DatagramPacket(ByteArray(1500), 1500)
                val (clientAddress, clientPort) = receiver.receivePing(incomingPacket)
                logger.debug("Получено PING от {}:{}", clientAddress, clientPort)
                val request = receiver.receiveRequest(clientAddress, clientPort)
                logger.info("Получен запрос: {}", request.commandName)
                val response = requestManager.dispatch(request)
                val responsePackets = utils.RUDPPacketSplitter(response)
                sender.sendResponse(responsePackets, clientAddress, clientPort)
                logger.debug("Ответ отправлен клиенту {}:{}", clientAddress, clientPort)
            } catch (_: ServerTimeoutException) {
                logger.debug("Таймаут соединения, ожидание следующего клиента")
            } catch (e: Exception) {
                logger.error("Ошибка в цикле сервера: {}", e.message, e)
            }
        }
    }
}
