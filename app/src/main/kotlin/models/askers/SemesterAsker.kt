package ru.qwuadrixx.app.models.askers

import ru.qwuadrixx.app.models.Semester
import ru.qwuadrixx.app.utils.IConsole

/**
 * Класс, запрашивающий данные для создания экземпляра класса Semester
 * @author qwuadrixx
 */
class SemesterAsker(private val console: IConsole) : Asker<Semester?> {
    /**
     * Главный метод, для получения класса
     * @return экземпляр semester
     */
    override fun ask(): Semester? {
        val semester = askSemester()
        return semester
    }

    private fun askSemester(): Semester? {
        console.printLine("Пожалуйста: введите один из представленных семестров")
        console.printObject(Semester.entries.toTypedArray().toString())
        while (true) {
            try {
                val line = console.readLine()
                if (line.isEmpty()) return null

                val semester = Semester.valueOf(line)

                return semester
            } catch (e: IllegalArgumentException) {
                console.printError(e)
                console.printLine("Попробуйте снова:")
            }
        }
    }


}