package ru.qwuadrixx.commands

import net.requests.IRequest
import net.responses.CommandResponse
import net.responses.IResponse
import utils.ExitCode

abstract class ServerCommand {
    abstract fun execute(request: IRequest): IResponse
    open fun undo(): IResponse = CommandResponse(ExitCode.ERROR, "Команда не может быть отменена")
    open val isUndoable: Boolean = false
}
