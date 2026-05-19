package net.requests

import kotlinx.serialization.Serializable
import utils.CommandName

@Serializable
class DisableServerRequest(
    val serverId: Int,
    override val token: String,
    override val commandName: CommandName = CommandName.DISABLE_SERVER
) : IRequest