@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package ru.qwuadrixx.balancer.health

import net.packet.RUDPPacket
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.LoggerFactory
import ru.qwuadrixx.balancer.di.BalancerConfig
import ru.qwuadrixx.balancer.manager.INodeManager
import ru.qwuadrixx.balancer.selector.IServerNode
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.SocketTimeoutException

class NodeHealthChecker(
    private val nodeManager: INodeManager
) : KoinComponent, Runnable {

    private val config: BalancerConfig by inject()
    private val logger = LoggerFactory.getLogger(NodeHealthChecker::class.java)

    override fun run() {
        logger.info("NodeHealthChecker запущен (интервал {}мс)", config.healthCheckIntervalMs)
        while (!Thread.currentThread().isInterrupted) {
            nodeManager.nodes().filter { !it.isAvailable() }.forEach { checkNode(it) }
            try {
                Thread.sleep(config.healthCheckIntervalMs)
            } catch (_: InterruptedException) {
                break
            }
        }
    }

    private fun checkNode(node: IServerNode) {
        try {
            DatagramSocket().use { socket ->
                socket.soTimeout = config.socketTimeoutMs
                val ping = RUDPPacket.byteArrayBALANCERPING()
                socket.send(DatagramPacket(ping, ping.size, node.address.address, node.address.port))
                val ack = DatagramPacket(ByteArray(16), 16)
                socket.receive(ack)
                node.markAlive()
                logger.info("Узел {}:{} снова доступен", node.address.hostString, node.address.port)
            }
        } catch (_: SocketTimeoutException) {
            logger.debug("Узел {}:{} ещё недоступен", node.address.hostString, node.address.port)
        } catch (_: Exception) {
            logger.debug("Узел {}:{} ещё недоступен", node.address.hostString, node.address.port)
        }
    }
}