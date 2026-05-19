package ru.qwuadrixx.app.session

class UserSession {
    var login: String = ""
    var token: String = ""
    val isAuthenticated: Boolean get() = token.isNotEmpty()

    fun set(login: String, token: String) {
        this.login = login
        this.token = token
    }

    fun clear() {
        login = ""
        token = ""
    }
}