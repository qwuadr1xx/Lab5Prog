package net.requests

import kotlinx.serialization.Serializable
import utils.CommandName

@Serializable
class AddServerRequest(
    val port: Int,
    override val token: String,
    override val commandName: CommandName = CommandName.ADD_SERVER
) : IRequest