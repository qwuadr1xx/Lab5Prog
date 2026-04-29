package ru.qwuadrixx.commands

import net.requests.IRequest
import net.requests.UndoRequest
import net.responses.CommandResponse
import net.responses.IResponse
import utils.ExitCode

class UndoCommand(private val history: ArrayDeque<ServerCommand>) : ServerCommand() {
    override val isMutating = true
    override fun execute(request: IRequest): IResponse {
        request as UndoRequest
        val n = minOf(request.n, history.size)
        if (n == 0) return CommandResponse(ExitCode.ERROR, "История команд пуста или запрошено 0 отмен")
        val results = mutableListOf<String>()
        repeat(n) {
            val cmd = history.removeLast()
            val result = cmd.undo() as CommandResponse
            results.add(result.message)
        }
        return CommandResponse(ExitCode.OK, "Отменено команд: $n\n${results.joinToString("\n")}")
    }
}
