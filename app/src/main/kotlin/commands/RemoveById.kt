package ru.qwuadrixx.app.commands

import net.requests.RemoveByIdRequest
import net.responses.CommandResponse
import ru.qwuadrixx.app.client.IRUDPClient
import ru.qwuadrixx.app.console.IConsole
import utils.ExitCode
import utils.ensure

/**
 * Команда remove_by_id
 * @author qwuadrixx
 */
class RemoveById(private val rudpClient: IRUDPClient, private val console: IConsole) :
    Command(name = "remove_by_id", description = "Удалить элемент из коллекции по его id") {

    override fun execute(): ExitCode {
        console.printLine("Использование команды remove_by_id")
        while (true) {
            try {
                console.printLine("Введите id(больше 0):")
                val id = console.readLine().toInt()
                ensure(id > 0) { "Значение id должно быть больше 0" }
                val response = rudpClient.sendAndReceive(RemoveByIdRequest(id)) as CommandResponse
                if (response.message.isNotEmpty()) console.printObject(response.message)
                return response.exitCode
            } catch (e: Exception) {
                console.printError(e)
                console.printLine("Попробуйте снова:")
            }
        }
    }
}