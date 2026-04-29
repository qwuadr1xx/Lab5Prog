package ru.qwuadrixx.balancer.selector

import java.net.InetSocketAddress
import java.util.concurrent.atomic.AtomicBoolean

class ServerNode(host: String, port: Int) {
    val address = InetSocketAddress(host, port)
    private val alive = AtomicBoolean(true)

    fun isAvailable(): Boolean = alive.get()

    fun markDead() { alive.compareAndSet(true, false) }

    fun markAlive() { alive.set(true) }
}