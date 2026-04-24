package net.requests

import kotlinx.serialization.Serializable
import models.StudyGroup
import utils.CommandName

@Serializable
class InsertAtRequest(
    val index: Int,
    val studyGroup: StudyGroup,
    override val commandName: CommandName = CommandName.INSERT_AT
) : IRequest