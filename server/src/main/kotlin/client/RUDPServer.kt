package ru.qwuadrixx.client

import exception.ServerTimeoutException
import net.assembler.Assembler
import net.packet.RUDPPacket
import net.requests.IRequest
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.LoggerFactory
import ru.qwuadrixx.di.ServerConfig
import ru.qwuadrixx.managers.RequestManager
import utils.requestDeserializer
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketTimeoutException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
class RUDPServer(
    private val requestManager: RequestManager
) : KoinComponent {

    private val config: ServerConfig by inject()
    private val logger = LoggerFactory.getLogger(RUDPServer::class.java)

    private val socket: DatagramSocket = DatagramSocket(config.port).apply {
        soTimeout = config.socketTimeoutMs
    }

    private val sendLock = Any()

    private val clients = ConcurrentHashMap<String, LinkedBlockingQueue<ByteArray>>()

    private val processPool = Executors.newCachedThreadPool()
    private val sendPool = Executors.newFixedThreadPool(config.fixedPoolSize)

    private fun send(data: ByteArray, address: InetAddress, port: Int) {
        synchronized(sendLock) {
            socket.send(DatagramPacket(data, data.size, address, port))
        }
    }

    fun runner() {
        logger.info("Сервер запущен на порту {}", config.port)
        val buffer = DatagramPacket(ByteArray(1500), 1500)
        while (true) {
            try {
                socket.receive(buffer)
                val bytes = buffer.data.copyOf(buffer.length)
                val addr = buffer.address
                val port = buffer.port
                val key = "$addr:$port"

                when {
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
                // продолжаем ожидание
            } catch (e: Exception) {
                logger.error("Ошибка в основном цикле: {}", e.message, e)
            }
        }
    }

    private fun handleClient(addr: InetAddress, port: Int, key: String, queue: LinkedBlockingQueue<ByteArray>) {
        try {
            val request = receiveRequest(queue, addr, port)
            logger.info("Получен запрос: {}", request.commandName)

            processPool.submit {
                try {
                    val response = requestManager.dispatch(request)
                    val packets = utils.RUDPPacketSplitter(response)
                    sendPool.submit {
                        try {
                            sendResponse(packets, queue, addr, port)
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

    private fun receiveRequest(queue: LinkedBlockingQueue<ByteArray>, addr: InetAddress, port: Int): IRequest {
        val assembler = Assembler()
        while (true) {
            val bytes = queue.poll(config.socketTimeoutMs.toLong(), TimeUnit.MILLISECONDS)
                ?: throw ServerTimeoutException("Таймаут получения запроса от $addr:$port")
            if (bytes.size < RUDPPacket.HEADING) continue
            val packet = RUDPPacket.fromByteArray(bytes)
            if (packet.length > 0) {
                send(RUDPPacket.byteArrayACK(), addr, port)
                assembler.addPacket(packet)
                if (assembler.isComplete(packet.uuid)) {
                    return requestDeserializer(assembler.assemble(packet.uuid))
                }
            }
        }
    }

    private fun sendResponse(
        packets: List<RUDPPacket>,
        queue: LinkedBlockingQueue<ByteArray>,
        addr: InetAddress,
        port: Int
    ) {
        for (packet in packets) {
            var acked = false
            repeat(config.maxRetries) {
                if (!acked) {
                    send(packet.toByteArray(), addr, port)
                    val ackBytes = queue.poll(config.socketTimeoutMs.toLong(), TimeUnit.MILLISECONDS)
                    if (ackBytes != null && RUDPPacket.isByteArrayACK(ackBytes)) {
                        acked = true
                    }
                }
            }
            if (!acked) throw ServerTimeoutException("Нет ACK от клиента $addr:$port")
        }
    }
}
