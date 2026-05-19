package ru.qwuadrixx.commands

import net.requests.IRequest
import net.responses.CommandResponse
import net.responses.IResponse
import ru.qwuadrixx.managers.ICollectionManager
import utils.ExitCode

class ShowCommand(private val cm: ICollectionManager) : ServerCommand() {
    override fun execute(request: IRequest, userId: Long): IResponse {
        val content = if (cm.collection.isEmpty()) "Коллекция пуста"
        else cm.collection.joinToString("\n")
        return CommandResponse(ExitCode.OK, content)
    }
}