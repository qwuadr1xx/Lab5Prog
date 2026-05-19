package net.responses

import kotlinx.serialization.Serializable
import utils.ExitCode

@Serializable
data class AddServerResponse(
    override val exitCode: ExitCode,
    val message: String = "",
    val serverId: Int = -1
) : IResponse