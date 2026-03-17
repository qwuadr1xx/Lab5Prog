package ru.qwuadrixx.app.utils

import ru.qwuadrixx.app.exception.ValidationException
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

/**
 * Обертка для валидации данных
 * @param condition
 * @throws ValidationException
 */
@OptIn(ExperimentalContracts::class)
inline fun ensure(condition: Boolean, lazyMessage: () -> String) {
    contract {
        returns() implies condition
    }
    if (!condition) {
        throw ValidationException(lazyMessage())
    }
}

/**
 * Обертка для валидации данных при значении null
 * @throws ValidationException
 */
@OptIn(ExperimentalContracts::class)
inline fun <T : Any> ensureNotNull(value: T?, lazyMessage: () -> Any): T {
    contract {
        returns() implies (value != null)
    }

    if (value == null) {
        val message = lazyMessage()
        throw ValidationException(message.toString())
    } else {
        return value
    }
}

