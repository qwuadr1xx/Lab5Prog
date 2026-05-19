package net.requests

import kotlinx.serialization.Serializable
import utils.CommandName

@Serializable
class InfoRequest(
    override val token: String,
    override val commandName: CommandName = CommandName.INFO
) : IRequest