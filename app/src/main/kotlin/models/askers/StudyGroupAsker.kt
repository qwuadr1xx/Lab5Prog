package ru.qwuadrixx.app.models.askers

import ru.qwuadrixx.app.exception.ScriptErrorException
import ru.qwuadrixx.app.exception.ValidationException
import ru.qwuadrixx.app.models.Coordinates
import ru.qwuadrixx.app.models.Person
import ru.qwuadrixx.app.models.Semester
import ru.qwuadrixx.app.models.StudyGroup
import ru.qwuadrixx.app.utils.IConsole
import ru.qwuadrixx.app.utils.ensure

/**
 * Класс, запрашивающий данные для создания экземпляра класса StudyGroup
 * @author qwuadrixx
 */
class StudyGroupAsker(private val console: IConsole) : Asker<StudyGroup> {
    /**
     * Главный метод, для получения класса
     * @return экземпляр studyGroup
     */
    override fun ask(): StudyGroup {
        val name = askName()
        val coordinates = askCoordinates()
        val studentsCount = askStudentsCount()
        val expelledStudents = askExpelledStudents()
        val averageMark = askAverageMark()
        val semester = askSemester()
        val groupAdmin = askGroupAdmin()
        return StudyGroup(name = name, coordinates = coordinates, studentsCount = studentsCount,
            expelledStudents = expelledStudents, averageMark = averageMark, semesterEnum = semester,
            groupAdmin = groupAdmin)
    }

    fun ask(id: Int): StudyGroup {
        val name = askName()
        val coordinates = askCoordinates()
        val studentsCount = askStudentsCount()
        val expelledStudents = askExpelledStudents()
        val averageMark = askAverageMark()
        val semester = askSemester()
        val groupAdmin = askGroupAdmin()
        return StudyGroup(id = id, name = name, coordinates = coordinates, studentsCount = studentsCount,
            expelledStudents = expelledStudents, averageMark = averageMark, semesterEnum = semester,
            groupAdmin = groupAdmin)
    }

    private fun askName(): String {
        console.printLine("Пожалуйста, введите название группы(не пустое):")
        while (true) {
            try {
                val name = console.readLine()

                ensure(name.isNotBlank()) { "Name должно быть не пустым" }

                return name
            } catch (e: ValidationException) {
                if (console.fileMode) throw ScriptErrorException(e.message)
                console.printError(e)
                console.printLine("Попробуйте снова:")
            }
        }
    }

    private fun askCoordinates(): Coordinates {
        return CoordinatesAsker(console).ask()
    }

    private fun askStudentsCount(): Long? {
        console.printLine("Пожалуйста, введите число(положительное) студентов:")
        while (true) {
            try {
                val line = console.readLine()
                if (line.isEmpty()) return null

                val studentsCount = line.toLong()

                ensure(studentsCount > 0) { "StudentsCount должен быть > 0 или null" }
                return studentsCount
            } catch (e: ValidationException) {
                if (console.fileMode) throw ScriptErrorException(e.message)
                console.printError(e)
                console.printLine("Попробуйте снова:")
            } catch (e: NumberFormatException) {
                if (console.fileMode) throw ScriptErrorException(e.message)
                console.printError(e)
                console.printLine("Попробуйте снова:")
            }
        }
    }

    private fun askExpelledStudents(): Int {
        console.printLine("Пожалуйста, введите число(положительное) исключенных студентов:")
        while (true) {
            try {
                val expelledStudents = console.readLine().toInt()

                ensure(expelledStudents > 0) { "ExpelledStudents должен быть > 0" }

                return expelledStudents
            } catch (e: ValidationException) {
                if (console.fileMode) throw ScriptErrorException(e.message)
                console.printError(e)
                console.printLine("Попробуйте снова:")
            } catch (e: NumberFormatException) {
                if (console.fileMode) throw ScriptErrorException(e.message)
                console.printError(e)
                console.printLine("Попробуйте снова:")
            }
        }
    }

    private fun askAverageMark(): Long {
        console.printLine("Пожалуйста, введите среднюю оценку(положительную) студентов:")
        while (true) {
            try {
                val averageMark = console.readLine().toLong()

                ensure(averageMark > 0) { "AverageMark должен быть > 0" }

                return averageMark
            } catch (e: ValidationException) {
                if (console.fileMode) throw ScriptErrorException(e.message)
                console.printError(e)
                console.printLine("Попробуйте снова:")
            } catch (e: NumberFormatException) {
                if (console.fileMode) throw ScriptErrorException(e.message)
                console.printError(e)
                console.printLine("Попробуйте снова:")
            }
        }
    }

    private fun askSemester(): Semester? {
        return SemesterAsker(console).ask()
    }

    private fun askGroupAdmin(): Person? {
        console.printLine("Введите 1, если будет администратор группы")
        if (console.readLine().toIntOrNull() == 1) {
            return PersonAsker(console).ask()
        }
        return null
    }

}