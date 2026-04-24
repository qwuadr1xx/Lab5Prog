package ru.qwuadrixx.parsers

class LineReader(private val lines: List<String>) {
    private var index = 0

    fun readLine(): String =
        if (index < lines.size) lines[index++].trim()
        else throw NoSuchElementException("Скрипт завершился неожиданно — ожидались ещё данные")

    fun hasNext(): Boolean = index < lines.size
}
