package net.requests

import utils.CommandName
import java.io.Serializable

interface IRequest : Serializable{
    val commandName: CommandName
}