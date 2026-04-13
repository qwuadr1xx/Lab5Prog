package net.requests

import kotlinx.serialization.Serializable
import utils.CommandName

@Serializable
class CountGreaterThanAverageMarkRequest(override val commandName: CommandName = CommandName.COUNT_GREATER_THAN_AVERAGE_MARK) :
    IRequest