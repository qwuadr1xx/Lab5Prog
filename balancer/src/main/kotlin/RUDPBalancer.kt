package ru.qwuadrixx.balancer

import exception.ServerTimeoutException
import net.assembler.Assembler
import net.assembler.IAssembler
import net.packet.RUDPPacket
import net.responses.IResponse
import org.slf4j.LoggerFactory
import ru.qwuadrixx.balancer.forwarder.RequestForwarder
import ru.qwuadrixx.balancer.forwarder.ResponseForwarder
import ru.qwuadrixx.balancer.forwarder.ServerResponseReceiver
import ru.qwuadrixx.balancer.receiver.BalancerReceiver
import ru.qwuadrixx.balancer.selector.NodeSelector
import ru.qwuadrixx.balancer.selector.ServerNode
import utils.RUDPPacketSplitter
import java.net.DatagramPacket
import java.net.DatagramSocket
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
class RUDPBalancer(
    private val port: Int,
    private val nodes: List<ServerNode>,
    private val maxRetries: Int,
    socketTimeoutMs: Int,
    pingTimeoutMs: Int
) {
    private val inboundSocket = DatagramSocket(port).apply { soTimeout = socketTimeoutMs }
    private val backendSocket = DatagramSocket().apply { soTimeout = socketTimeoutMs }
    private val clientAssembler: IAssembler = Assembler()
    private val serverAssembler: IAssembler = Assembler()
    private val logger = LoggerFactory.getLogger(RUDPBalancer::class.java)

    private val nodeSelector = NodeSelector(nodes)
    private val receiver = BalancerReceiver(inboundSocket, clientAssembler, maxRetries)
    private val requestForwarder = RequestForwarder(backendSocket, maxRetries, pingTimeoutMs)
    private val serverResponseReceiver = ServerResponseReceiver(backendSocket, serverAssembler, maxRetries)
    private val responseForwarder = ResponseForwarder(inboundSocket, maxRetries)

    fun start() {
        logger.info("Балансировщик запущен на порту {}", port)
        while (true) {
            try {
                val incomingPacket = DatagramPacket(ByteArray(1500), 1500)
                val (clientAddress, clientPort) = receiver.receivePing(incomingPacket)
                logger.debug("PING от {}:{}", clientAddress, clientPort)

                val request = receiver.receiveRequest(clientAddress, clientPort)
                logger.info("Запрос: {}", request.commandName)

                val response = forwardWithRetry(RUDPPacketSplitter(request))

                responseForwarder.sendResponse(RUDPPacketSplitter(response), clientAddress, clientPort)
                logger.debug("Ответ отправлен клиенту {}:{}", clientAddress, clientPort)
            } catch (_: ServerTimeoutException) {
                logger.debug("Таймаут соединения, ожидание следующего клиента")
            } catch (e: Exception) {
                logger.error("Ошибка в цикле балансировщика: {}", e.message, e)
            }
        }
    }

    private fun forwardWithRetry(requestPackets: List<RUDPPacket>): IResponse {
        repeat(nodes.size) {
            val node = nodeSelector.select()
            try {
                logger.debug("Выбран узел {}:{}", node.address.hostString, node.address.port)
                requestForwarder.sendPing(node.address.address, node.address.port)
                requestForwarder.sendRequest(requestPackets, node.address.address, node.address.port)
                return serverResponseReceiver.receiveResponse(node.address.address, node.address.port)
            } catch (e: ServerTimeoutException) {
                node.markDead()
                logger.warn("Узел {}:{} недоступен, пробуем следующий", node.address.hostString, node.address.port)
            }
        }
        throw ServerTimeoutException("Все доступные узлы недоступны")
    }
}