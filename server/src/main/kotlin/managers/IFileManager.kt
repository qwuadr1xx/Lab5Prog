package ru.qwuadrixx.managers

import models.StudyGroup

interface IFileManager {
    val fileName: String
    fun writeCollection(collection: Collection<StudyGroup>): String
    fun readCollection(): Pair<String, List<StudyGroup>>?
    fun clearVersion(collection: Collection<StudyGroup>)
}