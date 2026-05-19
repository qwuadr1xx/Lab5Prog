package net.requests

import kotlinx.serialization.Serializable
import utils.CommandName

@Serializable
class AverageOfAverageMarkRequest(
    override val token: String,
    override val commandName: CommandName = CommandName.AVERAGE_OF_AVERAGE_MARK
) : IRequest