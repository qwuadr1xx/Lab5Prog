package ru.qwuadrixx.balancer.process

interface IProcessManager {
    val processes: Map<Int, Process>
    fun addServer(port: Int): Int
    fun removeServer(id: Int)
}