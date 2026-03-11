package ru.qwuadrixx.app.models.askers

import ru.qwuadrixx.app.exception.ScriptErrorException
import ru.qwuadrixx.app.exception.ValidationException
import ru.qwuadrixx.app.models.Person
import ru.qwuadrixx.app.utils.IConsole
import ru.qwuadrixx.app.utils.ensure
import ru.qwuadrixx.app.utils.stringToDate
import java.time.format.DateTimeParseException
import java.util.Date

/**
 * Класс, запрашивающий данные для создания экземпляра класса Person
 * @author qwuadrixx
 */
class PersonAsker(private val console: IConsole) : Asker<Person> {
    /**
     * Главный метод, для получения класса
     * @return экземпляр person
     */
    override fun ask(): Person {
        val name = askName()
        val date = askDate()
        val height = askHeight()
        val passportID = askPassportID()
        return Person(name, date, height, passportID)
    }

    private fun askName(): String {
        console.printLine("Пожалуйста, введите имя человека(не пустое):")
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

    private fun askDate(): Date? {
        console.printLine("Пожалуйста, введите дату рождения в виде dd:MM:yyyy HH:mm:")
        while (true) {
            try {
                val line = console.readLine()
                if (line.isEmpty()) return null

                val date = stringToDate(line)
                return date
            } catch (e: DateTimeParseException) {
                if (console.fileMode) throw ScriptErrorException(e.message)
                console.printError(e)
                console.printLine("Попробуйте снова в формате dd:MM:yyyy HH:mm:")
            }
        }
    }

    private fun askHeight(): Double? {
        console.printLine("Пожалуйста, введите рост(больше 0):")
        while (true) {
            try {
                val line = console.readLine()
                if (line.isEmpty()) return null

                val height = line.toDouble()
                ensure(height > 0) { "Height должен быть > 0" }

                return height
            } catch (e: NumberFormatException) {
                if (console.fileMode) throw ScriptErrorException(e.message)
                console.printError(e)
                console.printLine("Попробуйте снова:")
            }
        }
    }

    private fun askPassportID(): String? {
        console.printLine("Пожалуйста, введите passportID:")
        while (true) {
            try {
                val passportID = console.readLine()
                if (passportID.isEmpty()) return null

                ensure(passportID.length in 7..48) {
                    "PassportID не может быть пустым, его длина должна быть между 7 и 48" }
                return passportID
            } catch (e: ValidationException) {
                if (console.fileMode) throw ScriptErrorException(e.message)
                console.printError(e)
                console.printLine("Попробуйте снова:")
            }
        }
    }
}