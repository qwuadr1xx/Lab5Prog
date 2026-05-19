package ru.qwuadrixx.commands

import net.requests.CountLessThanAverageMarkRequest
import net.requests.IRequest
import net.responses.CommandResponse
import net.responses.IResponse
import ru.qwuadrixx.managers.ICollectionManager
import utils.ExitCode

class CountLessThanAverageMarkCommand(private val cm: ICollectionManager) : ServerCommand() {
    override fun execute(request: IRequest, userId: Long): IResponse {
        request as CountLessThanAverageMarkRequest
        val count = cm.countAverageMarkLessThen(request.averageMark)
        return CommandResponse(ExitCode.OK, "Количество элементов с averageMark < ${request.averageMark}: $count")
    }
}