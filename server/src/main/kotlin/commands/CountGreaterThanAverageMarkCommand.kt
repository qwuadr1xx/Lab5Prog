package ru.qwuadrixx.commands

import net.requests.CountGreaterThanAverageMarkRequest
import net.requests.IRequest
import net.responses.CommandResponse
import net.responses.IResponse
import ru.qwuadrixx.managers.ICollectionManager
import utils.ExitCode

class CountGreaterThanAverageMarkCommand(private val cm: ICollectionManager) : ServerCommand() {
    override fun execute(request: IRequest, userId: Long): IResponse {
        request as CountGreaterThanAverageMarkRequest
        val count = cm.countAverageMarkGreaterThen(request.averageMark)
        return CommandResponse(ExitCode.OK, "Количество элементов с averageMark > ${request.averageMark}: $count")
    }
}