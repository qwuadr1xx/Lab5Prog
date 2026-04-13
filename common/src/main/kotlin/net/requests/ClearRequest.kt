package net.requests

import kotlinx.serialization.Serializable
import utils.CommandName

@Serializable
class ClearRequest(override val commandName: CommandName = CommandName.CLEAR) : IRequest