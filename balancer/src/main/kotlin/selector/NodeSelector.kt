package ru.qwuadrixx.balancer.selector

import exception.ServerTimeoutException
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.abs

class NodeSelector(private val nodes: List<ServerNode>) {
    private val counter = AtomicInteger(0)

    fun select(): ServerNode {
        val available = nodes.filter { it.isAvailable() }
        if (available.isEmpty()) throw ServerTimeoutException("Нет доступных серверных узлов")
        return available[(counter.getAndIncrement() and Int.MAX_VALUE) % available.size]
    }
}