package net.requests

import kotlinx.serialization.Serializable
import utils.CommandName

@Serializable
class UndoRequest(
    val n: Int,
    override val login: String,
    override val password: String,
    override val commandName: CommandName = CommandName.UNDO
) : IRequest