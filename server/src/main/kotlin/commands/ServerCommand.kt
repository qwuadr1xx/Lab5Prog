package ru.qwuadrixx.commands

import net.requests.IRequest
import net.responses.CommandResponse
import net.responses.IResponse
import utils.ExitCode

abstract class ServerCommand {
    abstract fun execute(request: IRequest): IResponse
    open val isUndoable: Boolean = false
    open val isMutating: Boolean get() = isUndoable
}
