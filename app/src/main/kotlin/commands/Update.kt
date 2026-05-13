package ru.qwuadrixx.app.commands

import net.requests.UpdateRequest
import net.responses.CommandResponse
import ru.qwuadrixx.app.client.IRUDPClient
import ru.qwuadrixx.app.console.IConsole
import ru.qwuadrixx.app.models.askers.StudyGroupAsker
import ru.qwuadrixx.app.session.UserSession
import utils.ExitCode
import utils.ensure

class Update(
    private val rudpClient: IRUDPClient,
    private val console: IConsole,
    private val session: UserSession
) : Command(name = "update", description = "Обновить значение элемента коллекции, id которого равен заданному") {

    override fun execute(): ExitCode {
        console.printLine("Использование команды update")
        while (true) {
            try {
                console.printLine("Введите id(больше 0):")
                val id = console.readLine().toInt()
                ensure(id > 0) { "Значение id должно быть больше 0" }
                val newStudyGroup = StudyGroupAsker(console).ask(id)
                val response = rudpClient.sendAndReceive(UpdateRequest(id, newStudyGroup, session.login, session.password)) as CommandResponse
                if (response.message.isNotEmpty()) console.printObject(response.message)
                return response.exitCode
            } catch (e: Exception) {
                console.printError(e)
                console.printLine("Попробуйте снова:")
            }
        }
    }
}
