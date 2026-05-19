package net.requests

import kotlinx.serialization.Serializable
import utils.CommandName

@Serializable
class RemoveServerRequest(
    val serverId: Int,
    override val token: String,
    override val commandName: CommandName = CommandName.REMOVE_SERVER
) : IRequest