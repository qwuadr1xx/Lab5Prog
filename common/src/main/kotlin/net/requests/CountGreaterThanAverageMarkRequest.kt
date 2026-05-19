package net.requests

import kotlinx.serialization.Serializable
import utils.CommandName

@Serializable
class CountGreaterThanAverageMarkRequest(
    val averageMark: Long,
    override val token: String,
    override val commandName: CommandName = CommandName.COUNT_GREATER_THAN_AVERAGE_MARK
) : IRequest