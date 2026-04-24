package ru.qwuadrixx.app.commands

import net.requests.RemoveLastRequest
import net.responses.CommandResponse
import ru.qwuadrixx.app.client.IRUDPClient
import ru.qwuadrixx.app.console.IConsole
import utils.ExitCode

/**
 * Команда remove_last
 * @author qwuadrixx
 */
class RemoveLast(private val rudpClient: IRUDPClient, private val console: IConsole) :
    Command(name = "remove_last", description = "Удалить последний элемент из коллекции") {

    override fun execute(): ExitCode {
        console.printLine("Использование команды remove_last")
        try {
            val response = rudpClient.sendAndReceive(RemoveLastRequest()) as CommandResponse
            if (response.message.isNotEmpty()) console.printObject(response.message)
            return response.exitCode
        } catch (e: Exception) {
            console.printError(e)
        }
        return ExitCode.ERROR
    }
}