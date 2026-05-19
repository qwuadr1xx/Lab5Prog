@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package ru.qwuadrixx.client

import exception.ServerTimeoutException
import net.packet.RUDPPacket
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.LoggerFactory
import ru.qwuadrixx.di.ServerConfig
import ru.qwuadrixx.managers.RequestManager
import utils.RUDPPacketSplitter
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketException
import java.net.SocketTimeoutException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

class RUDPServer(
    private val requestManager: RequestManager,
    private val socket: DatagramSocket,
    private val receiver: RUDPServerReceiver,
    private val sender: RUDPServerSender
) : KoinComponent {

    private val config: ServerConfig by inject()
    private val logger = LoggerFactory.getLogger(RUDPServer::class.java)

    private val clients = ConcurrentHashMap<String, LinkedBlockingQueue<ByteArray>>()
    private val processPool = Executors.newCachedThreadPool()
    private val sendPool = Executors.newFixedThreadPool(config.fixedPoolSize)

    fun runner() {
        logger.info("Сервер запущен на порту {}", socket.localPort)
        val buffer = DatagramPacket(ByteArray(1500), 1500)
        try {
            while (true) {
                try {
                    socket.receive(buffer)
                    val bytes = buffer.data.copyOf(buffer.length)
                    val addr = buffer.address
                    val port = buffer.port
                    val key = "$addr:$port"

                    when {
                        RUDPPacket.isByteArrayBALANCERPING(bytes) -> {
                            logger.debug("BALANCER PING от {}:{}", addr, port)
                            send(RUDPPacket.byteArrayACK(), addr, port)
                        }
                        RUDPPacket.isByteArrayPING(bytes) -> {
                            logger.debug("PING от {}:{}", addr, port)
                            send(RUDPPacket.byteArrayACK(), addr, port)
                            if (!clients.containsKey(key)) {
                                val queue = LinkedBlockingQueue<ByteArray>()
                                clients[key] = queue
                                Thread { handleClient(addr, port, key, queue) }
                                    .also { it.isDaemon = true }
                                    .start()
                            }
                        }
                        else -> clients[key]?.offer(bytes)
                    }
                } catch (_: SocketTimeoutException) {
                } catch (_: SocketException) {
                    break
                } catch (e: Exception) {
                    logger.error("Ошибка в основном цикле: {}", e.message, e)
                }
            }
        } finally {
            processPool.shutdown()
            sendPool.shutdown()
            processPool.awaitTermination(5, TimeUnit.SECONDS)
            sendPool.awaitTermination(5, TimeUnit.SECONDS)
            logger.info("Сервер остановлен")
        }
    }

    fun shutdown() {
        logger.info("Получен сигнал завершения, останавливаю сервер...")
        socket.close()
    }

    private fun send(data: ByteArray, address: InetAddress, port: Int) {
        socket.send(DatagramPacket(data, data.size, address, port))
    }

    private fun handleClient(addr: InetAddress, port: Int, key: String, queue: LinkedBlockingQueue<ByteArray>) {
        try {
            val request = receiver.receiveRequest(queue, addr, port)
            logger.info("Получен запрос: {}", request.commandName)

            processPool.submit {
                try {
                    val response = requestManager.dispatch(request)
                    val packets = RUDPPacketSplitter(response)
                    sendPool.submit {
                        try {
                            sender.sendResponse(packets, queue, addr, port)
                            logger.debug("Ответ отправлен {}:{}", addr, port)
                        } catch (e: Exception) {
                            logger.error("Ошибка отправки ответа: {}", e.message)
                        } finally {
                            clients.remove(key)
                        }
                    }
                } catch (e: Exception) {
                    logger.error("Ошибка обработки запроса: {}", e.message, e)
                    clients.remove(key)
                }
            }
        } catch (_: ServerTimeoutException) {
            logger.debug("Таймаут получения запроса от {}:{}", addr, port)
            clients.remove(key)
        } catch (e: Exception) {
            logger.error("Ошибка в потоке клиента {}:{}: {}", addr, port, e.message, e)
            clients.remove(key)
        }
    }
}