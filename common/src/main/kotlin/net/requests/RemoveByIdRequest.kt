package net.requests

import kotlinx.serialization.Serializable
import utils.CommandName

@Serializable
class RemoveByIdRequest(
    val id: Int,
    override val login: String,
    override val password: String,
    override val commandName: CommandName = CommandName.REMOVE_BY_ID
) : IRequest