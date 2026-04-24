package ru.qwuadrixx.app.commands

import net.requests.UndoRequest
import net.responses.CommandResponse
import ru.qwuadrixx.app.client.IRUDPClient
import ru.qwuadrixx.app.console.IConsole
import utils.ExitCode
import utils.ensure

/**
 * Команда undo
 * @author qwuadrixx
 */
class Undo(private val rudpClient: IRUDPClient, private val console: IConsole) :
    Command(name = "undo", description = "Отмена последних n команд") {

    override fun execute(): ExitCode {
        console.printLine("Использование команды undo")
        while (true) {
            try {
                console.printLine("Введите количество отмененных команд(больше 0):")
                val n = console.readLine().toInt()
                ensure(n > 0) { "Значение количества отмененных команд должно быть больше 0" }
                val response = rudpClient.sendAndReceive(UndoRequest(n)) as CommandResponse
                if (response.message.isNotEmpty()) console.printObject(response.message)
                return response.exitCode
            } catch (e: Exception) {
                console.printError(e)
                console.printLine("Попробуйте снова:")
            }
        }
    }
}