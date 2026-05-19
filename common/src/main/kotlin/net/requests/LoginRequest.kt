package net.requests

import kotlinx.serialization.Serializable
import utils.CommandName

@Serializable
class LoginRequest(
    val login: String,
    val password: String,
    override val commandName: CommandName = CommandName.LOGIN,
    override val token: String = ""
) : IRequest