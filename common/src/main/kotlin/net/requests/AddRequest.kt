package net.requests

import models.StudyGroup
import utils.CommandName

class AddRequest(val studyGroup: StudyGroup, override val commandName: CommandName = CommandName.ADD) : IRequest {

}