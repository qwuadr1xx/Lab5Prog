package ru.qwuadrixx.utils

import java.security.MessageDigest

fun hashPassword(password: String): String =
    MessageDigest.getInstance("SHA-384")
        .digest(password.toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }
