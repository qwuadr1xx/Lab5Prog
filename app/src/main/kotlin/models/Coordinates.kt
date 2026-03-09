package ru.qwuadrixx.app.models

import ru.qwuadrixx.app.utils.ensure

/**
 * Дата-класс Coordinates
 * @author qwuadrixx
 */
data class Coordinates(
    val x: Long, //Поле не может быть null
    val y: Double //Максимальное значение поля: 572, Поле не может быть null
) {

    init {
        ensure(y <= 572) { "Максимальное значение поля Y: 572" }
    }
}