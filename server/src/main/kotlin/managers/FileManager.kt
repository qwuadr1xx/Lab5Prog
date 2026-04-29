package ru.qwuadrixx.managers

import kotlinx.datetime.LocalDateTime
import models.Coordinates
import models.Person
import models.Semester
import models.StudyGroup
import org.slf4j.LoggerFactory
import ru.qwuadrixx.utils.StudyGroupFactory
import utils.stringToDate
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.PrintWriter
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.UUID
import java.util.stream.Collectors

class FileManager(override val fileName: String) : IFileManager {

    private val logger = LoggerFactory.getLogger(FileManager::class.java)
    private val tmpFile = File("$fileName.tmp")

    override fun writeCollection(collection: Collection<StudyGroup>): String {
        val version = UUID.randomUUID().toString()
        val fos = FileOutputStream(tmpFile)
        val lock = fos.channel.lock()
        try {
            PrintWriter(fos).use { writer ->
                writer.println("version:$version")
                collection.forEach { group -> writer.println(buildRow(group)) }
            }
        } finally {
            try { lock.release() } catch (_: Exception) {}
        }
        Files.move(tmpFile.toPath(), File(fileName).toPath(),
            StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
        logger.info("Записано {} элементов в файл '{}' (версия {})", collection.size, fileName, version)
        return version
    }

    override fun readCollection(): Pair<String, List<StudyGroup>>? {
        return try {
            val lines = File(fileName).readLines().filter { it.isNotBlank() }
            if (lines.isEmpty()) return Pair("", emptyList())

            val firstLine = lines[0]
            val version = if (firstLine.startsWith("version:")) firstLine.removePrefix("version:") else ""
            val dataLines = if (firstLine.startsWith("version:")) lines.drop(1) else lines

            val collection = dataLines.stream()
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

            logger.info("Прочитано {} элементов из файла '{}' (версия '{}')", collection.size, fileName, version)
            Pair(version, collection)
        } catch (e: FileNotFoundException) {
            logger.warn("Файл '{}' не найден — коллекция будет пустой", fileName)
            null
        } catch (e: Exception) {
            logger.error("Ошибка чтения файла '{}': {}", fileName, e.message, e)
            null
        }
    }

    override fun clearVersion(collection: Collection<StudyGroup>) {
        try {
            PrintWriter(File(fileName)).use { writer ->
                collection.forEach { group -> writer.println(buildRow(group)) }
            }
            logger.info("Поле версии очищено из файла '{}'", fileName)
        } catch (e: Exception) {
            logger.error("Ошибка очистки версии в файле '{}': {}", fileName, e.message, e)
        }
    }

    private fun buildRow(group: StudyGroup): String = listOf(
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
    ).joinToString(",")
}