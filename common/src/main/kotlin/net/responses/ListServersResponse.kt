package net.responses

import kotlinx.serialization.Serializable
import models.ServerNodeInfo
import utils.ExitCode

@Serializable
data class ListServersResponse(
    override val exitCode: ExitCode,
    val servers: List<ServerNodeInfo> = emptyList()
) : IResponse