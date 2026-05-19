package net.requests

import kotlinx.serialization.Serializable
import utils.CommandName
import utils.ServerFilterArg

@Serializable
class ListServersRequest(
    val filter: ServerFilterArg = ServerFilterArg.ALL,
    override val token: String,
    override val commandName: CommandName = CommandName.LIST_SERVERS
) : IRequest