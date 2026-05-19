package net.responses

import kotlinx.serialization.Serializable
import utils.ExitCode

@Serializable
data class RegisterResponse(
    override val exitCode: ExitCode,
    val message: String = "",
    val token: String = ""
) : IResponse