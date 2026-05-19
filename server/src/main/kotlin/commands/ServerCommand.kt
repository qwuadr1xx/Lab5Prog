package ru.qwuadrixx.commands

import net.requests.IRequest
import net.responses.IResponse

abstract class ServerCommand {
    abstract fun execute(request: IRequest, userId: Long): IResponse
    open val isUndoable: Boolean = false
    open val isMutating: Boolean get() = isUndoable
}