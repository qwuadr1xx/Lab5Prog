package net.responses

import kotlinx.serialization.Serializable
import utils.ExitCode

@Serializable
data class RemoveServerResponse(
    override val exitCode: ExitCode,
    val message: String = ""
) : IResponse