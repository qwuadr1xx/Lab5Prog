package ru.qwuadrixx.managers

import models.StudyGroup

interface IFileManager {
    val fileName: String
    fun writeCollection(collection: Collection<StudyGroup>)
    fun readCollection(): List<StudyGroup>?
}
