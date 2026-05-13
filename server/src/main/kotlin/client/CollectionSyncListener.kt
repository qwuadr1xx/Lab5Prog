package ru.qwuadrixx.client

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.LoggerFactory
import ru.qwuadrixx.di.ServerConfig
import ru.qwuadrixx.managers.ICollectionManager
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.SocketException

class CollectionSyncListener : KoinComponent, Runnable {

    private val config: ServerConfig by inject()
    private val cm: ICollectionManager by inject()
    private val logger = LoggerFactory.getLogger(CollectionSyncListener::class.java)

    override fun run() {
        val port = config.port + config.syncPortOffset
        try {
            DatagramSocket(port).use { socket ->
                logger.info("CollectionSyncListener запущен на порту {}", port)
                val buffer = DatagramPacket(ByteArray(1), 1)
                while (!Thread.currentThread().isInterrupted) {
                    try {
                        socket.receive(buffer)
                        logger.debug("Получен сигнал синхронизации, перезагружаю коллекцию из БД")
                        cm.reloadFromDb()
                    } catch (_: SocketException) {
                        break
                    } catch (e: Exception) {
                        logger.warn("Ошибка в CollectionSyncListener: {}", e.message)
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("Не удалось запустить CollectionSyncListener на порту {}: {}", port, e.message)
        }
    }
}
