package ru.qwuadrixx.parsers

import models.Coordinates
import models.Person
import models.Semester
import models.StudyGroup
import utils.ensure
import utils.stringToDate

object StudyGroupParser {

    fun parse(reader: LineReader): StudyGroup = parseFields(reader, fixedId = null)

    fun parse(reader: LineReader, id: Int): StudyGroup = parseFields(reader, fixedId = id)

    private fun parseFields(reader: LineReader, fixedId: Int?): StudyGroup {
        val name = reader.readLine()
        ensure(name.isNotBlank()) { "Name не может быть пустым" }

        val x = reader.readLine().toLong()
        val y = reader.readLine().toDouble()
        ensure(y <= 572) { "Максимальное значение координаты Y: 572" }

        val studentsCountLine = reader.readLine()
        val studentsCount = studentsCountLine.ifEmpty { null }
            ?.toLong()
            ?.also { ensure(it > 0) { "StudentsCount должен быть > 0" } }

        val expelledStudents = reader.readLine().toInt()
        ensure(expelledStudents > 0) { "ExpelledStudents должен быть > 0" }

        val averageMark = reader.readLine().toLong()
        ensure(averageMark > 0) { "AverageMark должен быть > 0" }

        val semesterLine = reader.readLine()
        val semester = semesterLine.ifEmpty { null }?.let { Semester.valueOf(it) }

        val adminFlag = reader.readLine().toIntOrNull()
        val groupAdmin = if (adminFlag == 1) parseAdmin(reader) else null

        return if (fixedId != null) {
            StudyGroup(
                id = fixedId,
                name = name,
                coordinates = Coordinates(x, y),
                studentsCount = studentsCount,
                expelledStudents = expelledStudents,
                averageMark = averageMark,
                semesterEnum = semester,
                groupAdmin = groupAdmin
            )
        } else {
            StudyGroup(
                name = name,
                coordinates = Coordinates(x, y),
                studentsCount = studentsCount,
                expelledStudents = expelledStudents,
                averageMark = averageMark,
                semesterEnum = semester,
                groupAdmin = groupAdmin
            )
        }
    }

    private fun parseAdmin(reader: LineReader): Person {
        val adminName = reader.readLine()
        ensure(adminName.isNotBlank()) { "Имя администратора не может быть пустым" }

        val birthdayLine = reader.readLine()
        val birthday = birthdayLine.ifEmpty { null }?.let { stringToDate(it) }

        val heightLine = reader.readLine()
        val height = heightLine.ifEmpty { null }
            ?.toDouble()
            ?.also { ensure(it > 0) { "Height должен быть > 0" } }

        val passportIdLine = reader.readLine()
        val passportId = passportIdLine.ifEmpty { null }
            ?.also { ensure(it.length in 7..48) { "PassportID длина от 7 до 48" } }

        return Person(adminName, birthday, height, passportId)
    }
}
