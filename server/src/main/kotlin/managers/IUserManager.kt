package ru.qwuadrixx.managers

interface IUserManager {
    fun register(login: String, password: String): Long
    fun login(login: String, password: String): UserInfo
}