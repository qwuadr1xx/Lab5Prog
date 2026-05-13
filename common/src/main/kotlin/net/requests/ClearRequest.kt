package net.requests

import kotlinx.serialization.Serializable
import utils.CommandName

@Serializable
class ClearRequest(
    override val login: String,
    override val password: String,
    override val commandName: CommandName = CommandName.CLEAR
) : IRequest