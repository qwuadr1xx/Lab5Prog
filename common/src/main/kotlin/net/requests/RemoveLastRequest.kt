package net.requests

import kotlinx.serialization.Serializable
import utils.CommandName

@Serializable
class RemoveLastRequest(override val commandName: CommandName = CommandName.REMOVE_LAST) : IRequest