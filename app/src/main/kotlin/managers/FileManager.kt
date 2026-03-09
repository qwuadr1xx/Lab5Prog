package ru.qwuadrixx.app.managers

import ru.qwuadrixx.app.exception.ValidationException
import ru.qwuadrixx.app.models.Coordinates
import ru.qwuadrixx.app.models.Person
import ru.qwuadrixx.app.models.Semester
import ru.qwuadrixx.app.models.StudyGroup
import ru.qwuadrixx.app.utils.IConsole
import ru.qwuadrixx.app.utils.stringToDate
import java.io.*
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.util.stream.Collectors

/**
 * Класс для работы с файлом
 * @author qwuadrixx
 */
class FileManager(
    override val console: IConsole,
    override val fileName: String
) : IFileManager {

    /**
     * Метод для записи коллекции в файл
     */
    override fun writeCollection(collection: Collection<StudyGroup>) {
        val writer = PrintWriter(File(fileName))

        collection.forEach {
            val row = listOf(
                it.id.toString(),
                it.name,
                it.coordinates.x.toString(),
                it.coordinates.y.toString(),
                it.creationDate.toString(),
                it.studentsCount?.toString() ?: "",
                it.expelledStudents.toString(),
                it.averageMark.toString(),
                it.semesterEnum?.name ?: "",
                it.groupAdmin?.name ?: "",
                it.groupAdmin?.birthday?.toString() ?: "",
                it.groupAdmin?.height?.toString() ?: "",
                it.groupAdmin?.passportID ?: ""
            )
            writer.println(row.joinToString(","))
        }

        writer.close()
    }

    /**
     * Метод для записи коллекции из файла
     */
    override fun readCollection(): Collection<StudyGroup>? {
        try {
            val reader = InputStreamReader(FileInputStream(fileName), StandardCharsets.UTF_8)

            val collection = reader.readLines().stream()
                .filter { it.isNotBlank() }
                .map { it ->
                    val parts = it.split(',', ignoreCase = false, limit = 13)

                    val id = parts[0]
                    val name = parts[1]
                    val coordinatesX = parts[2]
                    val coordinatesY = parts[3]
                    val creationDate = parts[4]
                    val studentsCount = parts[5]
                    val expelledStudents = parts[6]
                    val averageMark = parts[7]
                    val semesterEnum = parts[8]
                    val personName = parts[9]
                    val personBirthday = parts[10]
                    val personHeight = parts[11]
                    val personPassportID = parts[12]

                    StudyGroup(
                        id = id.toInt(),
                        name = name,
                        coordinates = Coordinates(
                            x = coordinatesX.toLong(),
                            y = coordinatesY.toDouble()
                        ),
                        creationDate = LocalDateTime.parse(creationDate),
                        studentsCount = studentsCount.ifEmpty { null }?.toLong(),
                        expelledStudents = expelledStudents.toInt(),
                        averageMark = averageMark.toLong(),
                        semesterEnum = semesterEnum.ifEmpty { null }
                            ?.let { Semester.valueOf(it) },
                        groupAdmin = personName.ifEmpty { null }
                            ?.let { pName ->
                                Person(
                                    name = pName,
                                    birthday = personBirthday.ifEmpty { null }
                                        ?.let { stringToDate(it) },
                                    height = personHeight.ifEmpty { null }?.toDouble(),
                                    passportID = personPassportID.ifEmpty { null }
                                )
                            }
                    )
                }
                .peek { StudyGroup.syncIdGenerator(it.id) }
                .collect(Collectors.toList())

            reader.close()
            return collection
        } catch (e: FileNotFoundException) {
            console.printError(e)
            console.printLine("Файл не найден, попробуйте ввести валидное название")
        } catch (e: SecurityException) {
            console.printError(e)
            console.printLine("Недостаточно прав для чтения файла")
        } catch (e: ValidationException) {
            console.printError(e)
            console.printLine("Данные в файле не валидны")
        }
        return null
    }
}