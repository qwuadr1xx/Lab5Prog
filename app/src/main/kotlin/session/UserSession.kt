package ru.qwuadrixx.app.session

class UserSession {
    var login: String = ""
    var password: String = ""
    val isAuthenticated: Boolean get() = login.isNotEmpty()

    fun set(login: String, password: String) {
        this.login = login
        this.password = password
    }

    fun clear() {
        login = ""
        password = ""
    }
}
