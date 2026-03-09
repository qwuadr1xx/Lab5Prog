package ru.qwuadrixx.app.managers

import ru.qwuadrixx.app.models.StudyGroup
import ru.qwuadrixx.app.utils.IConsole

interface IFileManager {
    val console: IConsole
    val fileName: String

    fun writeCollection(collection: Collection<StudyGroup>)

    fun readCollection(): Collection<StudyGroup>?
}