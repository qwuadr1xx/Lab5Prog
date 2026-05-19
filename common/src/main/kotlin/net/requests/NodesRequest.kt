package net.requests

import kotlinx.serialization.Serializable
import utils.CommandName

@Serializable
class NodesRequest(
    override val token: String,
    override val commandName: CommandName = CommandName.NODES
) : IRequest