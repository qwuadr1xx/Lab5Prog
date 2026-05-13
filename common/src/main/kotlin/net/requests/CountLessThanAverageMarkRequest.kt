package net.requests

import kotlinx.serialization.Serializable
import utils.CommandName

@Serializable
class CountLessThanAverageMarkRequest(
    val averageMark: Long,
    override val login: String,
    override val password: String,
    override val commandName: CommandName = CommandName.COUNT_LESS_THAN_AVERAGE_MARK
) : IRequest