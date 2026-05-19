package ru.qwuadrixx.app.commands

import net.requests.RegisterRequest
import net.responses.RegisterResponse
import ru.qwuadrixx.app.client.IRUDPClient
import ru.qwuadrixx.app.console.IConsole
import ru.qwuadrixx.app.session.UserSession
import utils.ExitCode

class Register(
    private val rudpClient: IRUDPClient,
    private val console: IConsole,
    private val session: UserSession
) : Command(name = "register", description = "Зарегистрироваться в системе") {

    override fun execute(): ExitCode {
        console.printLine("Использование команды register")
        try {
            console.printLine("Введите логин:")
            val login = console.readLine()
            console.printLine("Введите пароль:")
            val password = console.readLine()

            val response = rudpClient.sendAndReceive(RegisterRequest(login, password)) as RegisterResponse
            if (response.message.isNotEmpty()) console.printObject(response.message)

            if (response.exitCode == ExitCode.OK) {
                session.set(login, response.token)
                console.printLine("Регистрация успешна. Вы вошли как: $login")
            }
            return response.exitCode
        } catch (e: Exception) {
            console.printError(e)
        }
        return ExitCode.ERROR
    }
}