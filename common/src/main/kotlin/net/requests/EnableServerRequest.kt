package net.requests

import kotlinx.serialization.Serializable
import utils.CommandName

@Serializable
class EnableServerRequest(
    val serverId: Int,
    override val token: String,
    override val commandName: CommandName = CommandName.ENABLE_SERVER
) : IRequest