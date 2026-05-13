package net.requests

import kotlinx.serialization.Serializable
import utils.CommandName

@Serializable
class RegisterRequest(
    override val login: String,
    override val password: String,
    override val commandName: CommandName = CommandName.REGISTER
) : IRequest
