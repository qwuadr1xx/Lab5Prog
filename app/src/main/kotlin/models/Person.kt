package ru.qwuadrixx.app.models

import ru.qwuadrixx.app.utils.ensure
import java.util.Date

/**
 * Дата-класс Person
 * @author qwuadrixx
 */
data class Person(
    val name: String, //Поле не может быть null, Строка не может быть пустой
    val birthday: Date?, //Поле может быть null
    val height: Double?, //Поле может быть null, Значение поля должно быть больше 0
    val passportID: String? //Длина строки не должна быть больше 48, Строка не может быть пустой, Длина строки должна быть не меньше 7, Поле может быть null
) {
    init {
        ensure(name.isNotBlank()) { "Name должно быть не пустым" }
        ensure(height == null || height > 0) { "Height должен быть > 0" }
        ensure(passportID == null || (passportID.isNotBlank() && passportID.length in 7..48)) {
            "PassportID не может быть пустым, его длина должна быть между 7 и 48"
        }
    }
}