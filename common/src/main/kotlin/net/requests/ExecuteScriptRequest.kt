package net.requests

import kotlinx.serialization.Serializable
import utils.CommandName

@Serializable
class ExecuteScriptRequest(
    val lines: List<String>,
    override val token: String,
    override val commandName: CommandName = CommandName.EXECUTE_SCRIPT
) : IRequest