package ru.qwuadrixx.app.models.askers

interface Asker<T> {
    fun ask(): T
}