package ru.qwuadrixx.commands

import net.requests.IRequest
import net.responses.CommandResponse
import net.responses.IResponse
import ru.qwuadrixx.managers.ICollectionManager
import utils.ExitCode

class AverageOfAverageMarkCommand(private val cm: ICollectionManager) : ServerCommand() {
    override fun execute(request: IRequest): IResponse {
        val avg = cm.getAverageMarkFromAll()
        return CommandResponse(ExitCode.OK, "Среднее значение averageMark: $avg")
    }
}
