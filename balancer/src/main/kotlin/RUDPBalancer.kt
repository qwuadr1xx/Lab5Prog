@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package ru.qwuadrixx.balancer

import exception.ServerTimeoutException
import net.assembler.Assembler
import net.assembler.IAssembler
import net.packet.RUDPPacket
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.LoggerFactory
import ru.qwuadrixx.balancer.auth.AdminTokenCache
import ru.qwuadrixx.balancer.commands.BalancerCommandHandler
import ru.qwuadrixx.balancer.di.BalancerConfig
import ru.qwuadrixx.balancer.forwarder.RequestForwarder
import ru.qwuadrixx.balancer.forwarder.ResponseForwarder
import ru.qwuadrixx.balancer.forwarder.ServerResponseReceiver
import ru.qwuadrixx.balancer.manager.INodeManager
import ru.qwuadrixx.balancer.receiver.BalancerReceiver
import utils.RUDPPacketSplitter
import utils.TokenUtils
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketTimeoutException

class RUDPBalancer(
    private val nodeManager: INodeManager,
    private val tokenUtils: TokenUtils,
    private val adminTokenCache: AdminTokenCache,
    private val commandHandler: BalancerCommandHandler
) : KoinComponent {

    private val config: BalancerConfig by inject()
    private val inboundSocket = DatagramSocket(config.port).apply { soTimeout = config.socketTimeoutMs }
    private val backendSocket = DatagramSocket().apply { soTimeout = config.socketTimeoutMs }
    private val clientAssembler: IAssembler = Assembler()
    private val serverAssembler: IAssembler = Assembler()
    private val logger = LoggerFactory.getLogger(RUDPBalancer::class.java)

    private val receiver = BalancerReceiver(inboundSocket, clientAssembler, config.maxRetries)
    private val requestForwarder = RequestForwarder(backendSocket, config.maxRetries, config.pingTimeoutMs)
    private val serverResponseReceiver = ServerResponseReceiver(backendSocket, serverAssembler, config.maxRetries)
    private val responseForwarder = ResponseForwarder(inboundSocket, config.maxRetries)

    fun start() {
        logger.info("Балансировщик запущен на порту {}", config.port)
        val buffer = DatagramPacket(ByteArray(1500), 1500)
        while (true) {
            try {
                inboundSocket.receive(buffer)
                val bytes = buffer.data.copyOf(buffer.length)
                val addr = buffer.address
                val port = buffer.port

                when {
                    RUDPPacket.isByteArrayBALANCERPING(bytes) -> handleServerRegistration(addr, port)
                    RUDPPacket.isByteArrayPING(bytes) -> handleClientRequest(addr, port)
                    else -> logger.debug("Неизвестный пакет от {}:{}", addr, port)
                }
            } catch (_: SocketTimeoutException) {
            } catch (_: ServerTimeoutException) {
                logger.debug("Таймаут соединения, ожидание следующего клиента")
            } catch (e: Exception) {
                logger.error("Ошибка в цикле балансировщика: {}", e.message, e)
            }
        }
    }

    private fun handleServerRegistration(addr: InetAddress, port: Int) {
        nodeManager.register(addr.hostAddress, port)
        val ack = RUDPPacket.byteArrayACK()
        inboundSocket.send(DatagramPacket(ack, ack.size, addr, port))
    }

    private fun handleClientRequest(addr: InetAddress, port: Int) {
        val ack = RUDPPacket.byteArrayACK()
        inboundSocket.send(DatagramPacket(ack, ack.size, addr, port))
        logger.debug("PING от {}:{}", addr, port)

        val request = receiver.receiveRequest(addr, port)
        logger.info("Запрос: {}", request.commandName)

        if (request.token.isNotEmpty()) {
            tokenUtils.decode(request.token)?.takeIf { it.isAdmin }?.let { token ->
                adminTokenCache.put(token)
                logger.debug("Закеширован admin-токен: login={}", token.login)
            }
        }

        val response = if (commandHandler.handles(request.commandName)) {
            logger.info("Команда {} обрабатывается локально балансировщиком", request.commandName)
            commandHandler.execute(request)
        } else {
            forwardWithRetry(RUDPPacketSplitter(request))
        }
        responseForwarder.sendResponse(RUDPPacketSplitter(response), addr, port)
        logger.debug("Ответ отправлен клиенту {}:{}", addr, port)
    }

    private fun forwardWithRetry(requestPackets: List<RUDPPacket>): net.responses.IResponse {
        val nodes = nodeManager.nodes()
        repeat(nodes.size.coerceAtLeast(1)) {
            val node = nodeManager.select()
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