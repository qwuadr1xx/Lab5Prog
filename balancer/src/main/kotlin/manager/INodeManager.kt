package ru.qwuadrixx.balancer.manager

import ru.qwuadrixx.balancer.selector.IServerNode

interface INodeManager {
    fun register(host: String, port: Int): Int
    fun register(host: String, port: Int, id: Int): Int
    fun remove(nodeId: Int)
    fun findById(nodeId: Int): IServerNode?
    fun nodes(): List<IServerNode>
    fun select(): IServerNode
}