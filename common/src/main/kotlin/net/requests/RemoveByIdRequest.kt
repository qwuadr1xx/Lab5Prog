package net.requests

import kotlinx.serialization.Serializable
import utils.CommandName

@Serializable
class RemoveByIdRequest(
    val id: Int,
    override val commandName: CommandName = CommandName.REMOVE_BY_ID
) : IRequest