package ru.qwuadrixx.app.models.askers

import ru.qwuadrixx.app.exception.ValidationException
import ru.qwuadrixx.app.models.Coordinates
import ru.qwuadrixx.app.utils.IConsole
import ru.qwuadrixx.app.utils.ensure

/**
 * Класс, запрашивающий данные для создания экземпляра класса Coordinates
 * @author qwuadrixx
 */
class CoordinatesAsker(private val console: IConsole) : Asker<Coordinates> {
    /**
     * Главный метод, для получения класса
     * @return экземпляр coordinates
     */
    override fun ask(): Coordinates {
        val x = askX()
        val y = askY()
        return Coordinates(x, y)
    }

    private fun askX(): Long {
        console.printLine("Пожалуйста, введите координату X:")
        while (true) {
            try {
                val line = console.readLine()
                ensure(line.isNotEmpty()) { "Координата X должна быть задана" }

                return line.toLong()
            } catch (e: ValidationException) {
                console.printError(e)
                console.printLine("Попробуйте снова:")
            } catch (e: NumberFormatException) {
                console.printError(e)
                console.printLine("Попробуйте снова:")
            }
        }
    }

    private fun askY(): Double {
        console.printLine("Пожалуйста, введите координату Y:")
        while (true) {
            try {
                val line = console.readLine()
                ensure(line.isNotEmpty()) { "Координата Y должна быть задана" }

                val yCoordinate = line.toDouble()
                ensure(yCoordinate <= 572) { "Максимальное значение поля: 572" }
                return yCoordinate
            } catch (e: ValidationException) {
                console.printError(e)
                console.printLine("Попробуйте снова:")
            } catch (e: NumberFormatException) {
                console.printError(e)
                console.printLine("Попробуйте снова:")
            }
        }
    }
}