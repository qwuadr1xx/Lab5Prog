package utils

import kotlinx.serialization.Serializable

@Serializable
enum class CommandName {
    HELP,
    INFO,
    SHOW,
    ADD,
    UPDATE,
    REMOVE_BY_ID,
    CLEAR,
    SAVE,
    EXECUTE_SCRIPT,
    EXIT,
    INSERT_AT,
    REMOVE_LAST,
    ADD_IF_MAX,
    AVERAGE_OF_AVERAGE_MARK,
    COUNT_LESS_THAN_AVERAGE_MARK,
    COUNT_GREATER_THAN_AVERAGE_MARK
}