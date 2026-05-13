package ru.qwuadrixx.client

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.LoggerFactory
import ru.qwuadrixx.di.ServerConfig
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class CollectionSyncNotifier : KoinComponent {

    private val config: ServerConfig by inject()
    private val logger = LoggerFactory.getLogger(CollectionSyncNotifier::class.java)

    fun notifyPeers() {
        if (config.peers.isEmpty()) return
        config.peers.forEach { peer ->
            val parts = peer.split(":")
            if (parts.size != 2) return@forEach
            val host = parts[0]
            val peerMainPort = parts[1].toIntOrNull() ?: return@forEach
            val syncPort = peerMainPort + config.syncPortOffset
            try {
                DatagramSocket().use { socket ->
                    val payload = byteArrayOf(1)
                    socket.send(DatagramPacket(payload, payload.size, InetAddress.getByName(host), syncPort))
                }
                logger.debug("Отправлено уведомление о синхронизации на {}:{}", host, syncPort)
            } catch (e: Exception) {
                logger.warn("Не удалось уведомить пир {}:{} — {}", host, syncPort, e.message)
            }
        }
    }
}
