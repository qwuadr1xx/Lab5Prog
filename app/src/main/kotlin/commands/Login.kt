package ru.qwuadrixx.app.commands

import net.requests.LoginRequest
import net.responses.LoginResponse
import ru.qwuadrixx.app.client.IRUDPClient
import ru.qwuadrixx.app.console.IConsole
import ru.qwuadrixx.app.session.UserSession
import utils.ExitCode

class Login(
    private val rudpClient: IRUDPClient,
    private val console: IConsole,
    private val session: UserSession
) : Command(name = "login", description = "Авторизоваться в системе") {

    override fun execute(): ExitCode {
        console.printLine("Использование команды login")
        try {
            console.printLine("Введите логин:")
            val login = console.readLine()
            console.printLine("Введите пароль:")
            val password = console.readLine()

            val response = rudpClient.sendAndReceive(LoginRequest(login, password)) as LoginResponse
            if (response.message.isNotEmpty()) console.printObject(response.message)

            if (response.exitCode == ExitCode.OK) {
                session.set(login, password)
                console.printLine("Вы вошли как: $login")
            }
            return response.exitCode
        } catch (e: Exception) {
            console.printError(e)
        }
        return ExitCode.ERROR
    }
}
