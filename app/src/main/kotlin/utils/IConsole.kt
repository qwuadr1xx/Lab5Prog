package ru.qwuadrixx.app.utils

import java.io.BufferedReader

interface IConsole {
    var fileMode: Boolean
    var reader: BufferedReader

    fun readLine(): String

    fun printLine(line: String)

    fun printObject(obj: Any)

    fun printError(exc: Exception)

    fun printError(exc: Exception, message: String)

    fun setFileMode(fileName: String)

    fun setInteractiveMode()
}