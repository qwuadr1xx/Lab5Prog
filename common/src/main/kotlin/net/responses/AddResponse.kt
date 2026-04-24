package net.responses

import kotlinx.serialization.Serializable
import utils.ExitCode

@Serializable
class AddResponse(override val exitCode: ExitCode) : IResponse