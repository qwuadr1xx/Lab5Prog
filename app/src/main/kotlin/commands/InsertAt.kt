package ru.qwuadrixx.app.commands

import net.requests.InsertAtRequest
import net.responses.CommandResponse
import ru.qwuadrixx.app.client.IRUDPClient
import ru.qwuadrixx.app.console.IConsole
import ru.qwuadrixx.app.models.askers.StudyGroupAsker
import ru.qwuadrixx.app.session.UserSession
import utils.ExitCode
import utils.ensure

class InsertAt(
    private val rudpClient: IRUDPClient,
    private val console: IConsole,
    private val session: UserSession
) : Command(name = "insert_at", description = "Добавить новый элемент в заданную позицию") {

    override fun execute(): ExitCode {
        console.printLine("Использование команды insert_at")
        while (true) {
            try {
                console.printLine("Введите index(больше 0):")
                val index = console.readLine().toInt()
                ensure(index > 0) { "Значение index должно быть больше 0" }
                val studyGroup = StudyGroupAsker(console = console).ask()
                val response = rudpClient.sendAndReceive(InsertAtRequest(index, studyGroup, session.token)) as CommandResponse
                if (response.message.isNotEmpty()) console.printObject(response.message)
                return response.exitCode
            } catch (e: Exception) {
                console.printError(e)
                console.printLine("Попробуйте снова:")
            }
        }
    }
}
