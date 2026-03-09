package ru.qwuadrixx.app.utils

import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

/**
 * Класс консоли для взаимодействия с пользователем
 * @author qwuadrixx
 */
class Console(
    override var fileMode: Boolean = false,
    override var reader: BufferedReader = BufferedReader(InputStreamReader(System.`in`))
) : IConsole {
    /**
     * Метод для чтения из консоли/файла
     * @return прочтенная строка
     */
    override fun readLine(): String {
        return reader.readLine()?.trim() ?: ""
    }

    /**
     * Метод для вывода в консоль строки в интерактивном режиме
     * @param line
     */
    override fun printLine(line: String) {
        if (!fileMode) println(line)
    }

    /**
     * Метод для вывода в консоль объекта в интерактивном режиме
     * @param obj
     */
    override fun printObject(obj: Any) {
        println(obj)
    }

    /**
     * Метод для вывода в консоль ошибки
     * @param exc
     */
    override fun printError(exc: Exception) {
        val javaClass = exc.javaClass.simpleName
        val message = exc.message
        println("Произошла ошибка $javaClass, сообщение ошибки: $message")
    }

    /**
     * Метод переключения консоли в файловый режим
     * @param fileName
     */
    override fun setFileMode(fileName: String) {
        if (!File(fileName).exists()) throw FileNotFoundException("Файл $fileName не найден")
        reader = BufferedReader(InputStreamReader(FileInputStream(fileName), StandardCharsets.UTF_8))
        fileMode = true
    }

    /**
     * Метод переключения консоли в интерактивный режим
     */
    override fun setInteractiveMode() {
        reader = BufferedReader(InputStreamReader(System.`in`))
        fileMode = false
    }
}