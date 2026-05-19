package ru.qwuadrixx.balancer.utils

import java.util.concurrent.atomic.AtomicInteger

object IdGenerator {
    private val counter = AtomicInteger(0)

    fun getAndIncrement(): Int = counter.getAndIncrement()

    fun rollback() { counter.decrementAndGet() }

    fun get(): Int = counter.get()
}