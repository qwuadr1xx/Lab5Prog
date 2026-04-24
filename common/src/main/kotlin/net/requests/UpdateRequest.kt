package net.requests

import kotlinx.serialization.Serializable
import models.StudyGroup
import utils.CommandName

@Serializable
class UpdateRequest(
    val id: Int,
    val studyGroup: StudyGroup,
    override val commandName: CommandName = CommandName.UPDATE
) : IRequest