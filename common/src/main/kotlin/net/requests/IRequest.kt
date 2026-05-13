package net.requests

import kotlinx.serialization.Serializable
import utils.CommandName

@Serializable
sealed interface IRequest {
    val commandName: CommandName
    val login: String
    val password: String
}