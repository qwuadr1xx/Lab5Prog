package net.requests

import kotlinx.serialization.Serializable
import utils.CommandName

@Serializable
class ShowRequest(
    override val login: String,
    override val password: String,
    override val commandName: CommandName = CommandName.SHOW
) : IRequest