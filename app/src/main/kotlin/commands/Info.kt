package ru.qwuadrixx.app.commands

import net.requests.InfoRequest
import net.responses.CommandResponse
import ru.qwuadrixx.app.client.IRUDPClient
import ru.qwuadrixx.app.console.IConsole
import utils.ExitCode

/**
 * Команда info
 * @author qwuadrixx
 */
class Info(private val rudpClient: IRUDPClient, private val console: IConsole) : Command(
    name = "info",
    description = "Вывести в стандартный поток вывода информацию о коллекции " +
            "(тип, дата инициализации, количество элементов и т.д.)"
) {
    override fun execute(): ExitCode {
        console.printLine("Использование команды info")
        try {
            val response = rudpClient.sendAndReceive(InfoRequest()) as CommandResponse
            if (response.message.isNotEmpty()) console.printObject(response.message)
            return response.exitCode
        } catch (e: Exception) {
            console.printError(e)
        }
        return ExitCode.ERROR
    }
}