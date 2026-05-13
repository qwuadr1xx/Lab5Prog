package ru.qwuadrixx.app.commands

import net.requests.UndoRequest
import net.responses.CommandResponse
import ru.qwuadrixx.app.client.IRUDPClient
import ru.qwuadrixx.app.console.IConsole
import ru.qwuadrixx.app.session.UserSession
import utils.ExitCode
import utils.ensure

class Undo(
    private val rudpClient: IRUDPClient,
    private val console: IConsole,
    private val session: UserSession
) : Command(name = "undo", description = "Отмена последних n команд") {

    override fun execute(): ExitCode {
        console.printLine("Использование команды undo")
        while (true) {
            try {
                console.printLine("Введите количество отменённых команд(больше 0):")
                val n = console.readLine().toInt()
                ensure(n > 0) { "Значение количества отменённых команд должно быть больше 0" }
                val response = rudpClient.sendAndReceive(UndoRequest(n, session.login, session.password)) as CommandResponse
                if (response.message.isNotEmpty()) console.printObject(response.message)
                return response.exitCode
            } catch (e: Exception) {
                console.printError(e)
                console.printLine("Попробуйте снова:")
            }
        }
    }
}
