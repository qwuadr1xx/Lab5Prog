package net.requests

import kotlinx.serialization.Serializable
import models.StudyGroup
import utils.CommandName

@Serializable
class AddRequest(
    val studyGroup: StudyGroup,
    override val login: String,
    override val password: String,
    override val commandName: CommandName = CommandName.ADD
) : IRequest