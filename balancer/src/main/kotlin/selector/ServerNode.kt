package ru.qwuadrixx.balancer.selector

import ru.qwuadrixx.balancer.utils.IdGenerator
import java.net.InetSocketAddress
import java.time.Instant

class ServerNode(
    host: String,
    port: Int,
    isEnabled: Boolean = true,
    override val isMain: Boolean = false,
    id: Int? = null
) : IServerNode {

    override val id: Int = id ?: IdGenerator.getAndIncrement()
    override val address: InetSocketAddress = InetSocketAddress(host, port)
    override val createdAt: Instant = Instant.now()

    @Volatile
    override var isEnabled: Boolean = isEnabled
        private set

    @Volatile
    override var notAvailableSince: Instant? = null
        private set

    override fun isAvailable(): Boolean = notAvailableSince == null

    override fun enable() { isEnabled = true }

    override fun disable() { isEnabled = false }

    override fun markDead() {
        if (notAvailableSince == null) notAvailableSince = Instant.now()
    }

    override fun markAlive() {
        notAvailableSince = null
    }
}