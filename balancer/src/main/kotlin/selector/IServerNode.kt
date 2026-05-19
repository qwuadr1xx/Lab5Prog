package ru.qwuadrixx.balancer.selector

import java.net.InetSocketAddress
import java.time.Instant

interface IServerNode {
    val id: Int
    val address: InetSocketAddress
    val isEnabled: Boolean
    val isMain: Boolean
    val createdAt: Instant
    val notAvailableSince: Instant?
    fun isAvailable(): Boolean
    fun enable()
    fun disable()
    fun markDead()
    fun markAlive()
}