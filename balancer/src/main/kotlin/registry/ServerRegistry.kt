package ru.qwuadrixx.balancer.registry

import org.slf4j.LoggerFactory
import ru.qwuadrixx.balancer.selector.IServerNode
import ru.qwuadrixx.balancer.selector.ServerNode
import java.net.InetAddress
import java.util.concurrent.CopyOnWriteArrayList

class ServerRegistry(initialNodes: List<ServerNode> = emptyList()) {

    private val nodes = CopyOnWriteArrayList<IServerNode>(initialNodes)
    private val logger = LoggerFactory.getLogger(ServerRegistry::class.java)

    fun register(host: String, port: Int): Int = registerInternal(host, port, null)

    fun register(host: String, port: Int, id: Int): Int = registerInternal(host, port, id)

    private fun registerInternal(host: String, port: Int, id: Int?): Int {
        val resolvedIp = try {
            InetAddress.getByName(host).hostAddress
        } catch (_: Exception) {
            host
        }

        val existing = nodes.find { it.address.address.hostAddress == resolvedIp && it.address.port == port }
        if (existing != null) {
            existing.markAlive()
            logger.debug("Сервер {}:{} уже зарегистрирован, статус обновлён", resolvedIp, port)
            return existing.id
        }
        val isFirstNode = nodes.isEmpty()
        val node = ServerNode(resolvedIp, port, isMain = isFirstNode, id = id)
        nodes.add(node)
        logger.info("Сервер {}:{} зарегистрирован (id={}, main={}, всего: {})", resolvedIp, port, node.id, node.isMain, nodes.size)
        return node.id
    }

    fun removeById(nodeId: Int) {
        val node = nodes.find { it.id == nodeId } ?: return
        nodes.remove(node)
        logger.info("Сервер {}:{} удалён (осталось: {})", node.address.hostString, node.address.port, nodes.size)
    }

    fun nodes(): List<IServerNode> = nodes
}