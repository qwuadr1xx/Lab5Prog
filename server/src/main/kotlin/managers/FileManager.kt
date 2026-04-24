package ru.qwuadrixx.managers

import kotlinx.datetime.LocalDateTime
import models.Coordinates
import models.Person
import models.Semester
import models.StudyGroup
import ru.qwuadrixx.utils.StudyGroupFactory
import org.slf4j.LoggerFactory
import utils.stringToDate
import java.io.File
import java.io.FileNotFoundException
import java.io.PrintWriter
import java.util.stream.Collectors

class FileManager(override val fileName: String) : IFileManager {

    private val logger = LoggerFactory.getLogger(FileManager::class.java)

    override fun writeCollection(collection: Collection<StudyGroup>) {
        PrintWriter(File(fileName)).use { writer ->
            collection.forEach { group ->
                val row = listOf(
                    group.id.toString(),
                    group.name,
                    group.coordinates.x.toString(),
                    group.coordinates.y.toString(),
                    group.creationDate.toString(),
                    group.studentsCount?.toString() ?: "",
                    group.expelledStudents.toString(),
                    group.averageMark.toString(),
                    group.semesterEnum?.name ?: "",
                    group.groupAdmin?.name ?: "",
                    group.groupAdmin?.birthday?.toString() ?: "",
                    group.groupAdmin?.height?.toString() ?: "",
                    group.groupAdmin?.passportID ?: ""
                )
                writer.println(row.joinToString(","))
            }
        }
        logger.info("Записано {} элементов в файл '{}'", collection.size, fileName)
    }

    override fun readCollection(): List<StudyGroup>? {
        return try {
            File(fileName).inputStream().bufferedReader().use { reader ->
                reader.lines()
                    .filter { it.isNotBlank() }
                    .map { line ->
                        val parts = line.split(',', ignoreCase = false, limit = 13)
                        StudyGroup(
                            id = parts[0].toInt(),
                            name = parts[1],
                            coordinates = Coordinates(x = parts[2].toLong(), y = parts[3].toDouble()),
                            creationDate = LocalDateTime.parse(parts[4]),
                            studentsCount = parts[5].ifEmpty { null }?.toLong(),
                            expelledStudents = parts[6].toInt(),
                            averageMark = parts[7].toLong(),
                            semesterEnum = parts[8].ifEmpty { null }?.let { Semester.valueOf(it) },
                            groupAdmin = parts[9].ifEmpty { null }?.let { name ->
                                Person(
                                    name = name,
                                    birthday = parts[10].ifEmpty { null }?.let { stringToDate(it) },
                                    height = parts[11].ifEmpty { null }?.toDouble(),
                                    passportID = parts[12].ifEmpty { null }
                                )
                            }
                        )
                    }
                    .peek { StudyGroupFactory.syncIdGenerator(it.id) }
                    .collect(Collectors.toList())
            }.also { logger.info("Прочитано {} элементов из файла '{}'", it.size, fileName) }
        } catch (e: FileNotFoundException) {
            logger.warn("Файл '{}' не найден — коллекция будет пустой", fileName)
            null
        } catch (e: Exception) {
            logger.error("Ошибка чтения файла '{}': {}", fileName, e.message, e)
            null
        }
    }
}
