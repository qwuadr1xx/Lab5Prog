package ru.qwuadrixx.balancer.health

import org.slf4j.LoggerFactory
import ru.qwuadrixx.balancer.selector.ServerNode
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.SocketTimeoutException

class NodeHealthChecker(
    private val nodes: List<ServerNode>,
    private val healthPortOffset: Int,
    private val intervalMs: Long,
    private val timeoutMs: Int
) : Runnable {

    private val logger = LoggerFactory.getLogger(NodeHealthChecker::class.java)

    override fun run() {
        logger.info("NodeHealthChecker запущен (интервал {}мс, offset порта +{})", intervalMs, healthPortOffset)
        while (!Thread.currentThread().isInterrupted) {
            nodes.filter { !it.isAvailable() }.forEach { checkNode(it) }
            try {
                Thread.sleep(intervalMs)
            } catch (_: InterruptedException) {
                break
            }
        }
    }

    private fun checkNode(node: ServerNode) {
        val healthPort = node.address.port + healthPortOffset
        try {
            DatagramSocket().use { socket ->
                socket.soTimeout = timeoutMs
                socket.send(DatagramPacket(byteArrayOf(0), 1, node.address.address, healthPort))
                val ack = DatagramPacket(ByteArray(1), 1)
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