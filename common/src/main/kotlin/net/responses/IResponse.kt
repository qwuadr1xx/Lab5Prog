package net.responses

import kotlinx.serialization.Serializable
import utils.ExitCode

@Serializable
sealed interface IResponse {
    val exitCode: ExitCode
}