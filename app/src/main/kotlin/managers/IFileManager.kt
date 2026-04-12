package ru.qwuadrixx.app.managers

import models.StudyGroup
import ru.qwuadrixx.app.console.IConsole

interface IFileManager {
    val console: IConsole
    val fileName: String

    fun writeCollection(collection: Collection<StudyGroup>)

    fun readCollection(): Collection<StudyGroup>?

    fun readBytes(): ByteArray

    fun writeBytes(byteArray: ByteArray)
}