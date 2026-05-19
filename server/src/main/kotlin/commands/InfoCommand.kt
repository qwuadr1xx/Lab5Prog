package ru.qwuadrixx.commands

import net.requests.IRequest
import net.responses.CommandResponse
import net.responses.IResponse
import ru.qwuadrixx.managers.ICollectionManager
import utils.ExitCode

class InfoCommand(private val cm: ICollectionManager) : ServerCommand() {
    override fun execute(request: IRequest, userId: Long): IResponse {
        val info = buildString {
            appendLine("Тип коллекции: ${cm.collection.javaClass.simpleName}")
            appendLine("Дата инициализации: ${cm.lastInitTime}")
            appendLine("Дата последнего изменения: ${cm.lastEditTime}")
            append("Количество элементов: ${cm.collection.size}")
        }
        return CommandResponse(ExitCode.OK, info)
    }
}