package ru.qwuadrixx.app.commands

import net.requests.AddRequest
import net.responses.CommandResponse
import ru.qwuadrixx.app.client.IRUDPClient
import ru.qwuadrixx.app.console.IConsole
import ru.qwuadrixx.app.models.askers.StudyGroupAsker
import ru.qwuadrixx.app.session.UserSession
import utils.ExitCode

class Add(
    private val rudpClient: IRUDPClient,
    private val console: IConsole,
    private val session: UserSession
) : Command(name = "add", description = "Добавить новый элемент в коллекцию") {

    override fun execute(): ExitCode {
        console.printLine("Использование команды add")
        try {
            val studyGroup = StudyGroupAsker(console).ask()
            val response = rudpClient.sendAndReceive(AddRequest(studyGroup, session.token)) as CommandResponse
            if (response.message.isNotEmpty()) console.printObject(response.message)
            return response.exitCode
        } catch (e: Exception) {
            console.printError(e)
        }
        return ExitCode.ERROR
    }
}
