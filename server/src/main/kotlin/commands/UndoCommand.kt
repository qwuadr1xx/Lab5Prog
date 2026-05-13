package ru.qwuadrixx.commands

import net.requests.IRequest
import net.requests.UndoRequest
import net.responses.CommandResponse
import net.responses.IResponse
import ru.qwuadrixx.client.CollectionSyncNotifier
import ru.qwuadrixx.managers.ICollectionManager
import ru.qwuadrixx.repository.IHistoryRepository
import utils.ExitCode

class UndoCommand(
    private val historyRepo: IHistoryRepository,
    private val cm: ICollectionManager,
    private val syncNotifier: CollectionSyncNotifier
) : ServerCommand() {

    override fun execute(request: IRequest): IResponse {
        request as UndoRequest
        val steps = request.n
        if (steps <= 0) return CommandResponse(ExitCode.ERROR, "Количество шагов должно быть > 0")

        val snapshot = historyRepo.popAt(steps)
            ?: return CommandResponse(ExitCode.ERROR, "Недостаточно снапшотов для отката на $steps шаг(ов)")

        cm.restoreSnapshot(snapshot)
        syncNotifier.notifyPeers()
        return CommandResponse(ExitCode.OK, "Коллекция откатана на $steps шаг(ов) назад")
    }
}
