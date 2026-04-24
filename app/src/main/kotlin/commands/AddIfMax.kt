package ru.qwuadrixx.app.commands

import net.requests.AddIfMaxRequest
import net.responses.CommandResponse
import ru.qwuadrixx.app.client.IRUDPClient
import ru.qwuadrixx.app.console.IConsole
import ru.qwuadrixx.app.models.askers.StudyGroupAsker
import utils.ExitCode

/**
 * Команда add_if_max
 * @author qwuadrixx
 */
class AddIfMax(private val rudpClient: IRUDPClient, private val console: IConsole) :
    Command(
        name = "add_if_max",
        description = "Добавить новый элемент в коллекцию, если его значение превышает значение наибольшего элемента этой коллекции"
    ) {

    override fun execute(): ExitCode {
        console.printLine("Использование команды add_if_max")
        try {
            val studyGroup = StudyGroupAsker(console).ask()
            val response = rudpClient.sendAndReceive(AddIfMaxRequest(studyGroup)) as CommandResponse
            if (response.message.isNotEmpty()) console.printObject(response.message)
            return response.exitCode
        } catch (e: Exception) {
            console.printError(e)
        }
        return ExitCode.ERROR
    }
}