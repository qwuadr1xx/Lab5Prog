package net.requests

import kotlinx.serialization.Serializable
import utils.CommandName

@Serializable
class CountLessThanAverageMarkRequest(override val commandName: CommandName = CommandName.COUNT_LESS_THAN_AVERAGE_MARK) :
    IRequest