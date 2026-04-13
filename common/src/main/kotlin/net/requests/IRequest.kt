package net.requests

import kotlinx.serialization.Contextual
import utils.CommandName

interface IRequest {
    @Contextual
    val commandName: CommandName
}