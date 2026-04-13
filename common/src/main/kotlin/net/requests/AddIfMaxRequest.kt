package net.requests

import kotlinx.serialization.Serializable
import models.StudyGroup
import utils.CommandName

@Serializable
class AddIfMaxRequest(val studyGroup: StudyGroup, override val commandName: CommandName = CommandName.ADD_IF_MAX) :
    IRequest