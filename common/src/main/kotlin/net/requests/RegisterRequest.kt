package net.requests

import kotlinx.serialization.Serializable
import utils.CommandName

@Serializable
class RegisterRequest(
    val login: String,
    val password: String,
    override val commandName: CommandName = CommandName.REGISTER,
    override val token: String = ""
) : IRequest