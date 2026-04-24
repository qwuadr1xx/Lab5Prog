package net.requests

import kotlinx.serialization.Serializable
import utils.CommandName

@Serializable
class UndoRequest(
    val n: Int,
    override val commandName: CommandName = CommandName.UNDO
) : IRequest