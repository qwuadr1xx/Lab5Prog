package ru.qwuadrixx.app.console

import java.io.BufferedReader

interface IConsole {
    var fileMode: Boolean
    var reader: BufferedReader

    fun readLine(): String

    fun printLine(line: String)

    fun printObject(obj: Any)

    fun printError(exc: Exception)

    fun printError(message: String)

    fun setFileMode(fileName: String)

    fun setInteractiveMode()
}