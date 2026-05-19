package ru.qwuadrixx.balancer.manager

import exception.ServerTimeoutException
import ru.qwuadrixx.balancer.registry.ServerRegistry
import ru.qwuadrixx.balancer.selector.IServerNode
import java.util.concurrent.atomic.AtomicInteger

class NodeManager(private val registry: ServerRegistry) : INodeManager {

    private val counter = AtomicInteger(0)

    override fun register(host: String, port: Int): Int = registry.register(host, port)

    override fun register(host: String, port: Int, id: Int): Int = registry.register(host, port, id)

    override fun remove(nodeId: Int) = registry.removeById(nodeId)

    override fun findById(nodeId: Int): IServerNode? = registry.nodes().find { it.id == nodeId }

    override fun nodes(): List<IServerNode> = registry.nodes()

    override fun select(): IServerNode {
        val available = registry.nodes().filter { it.isEnabled && it.isAvailable() }
        if (available.isEmpty()) throw ServerTimeoutException("Нет доступных серверных узлов")
        return available[(counter.getAndIncrement() and Int.MAX_VALUE) % available.size]
    }
}