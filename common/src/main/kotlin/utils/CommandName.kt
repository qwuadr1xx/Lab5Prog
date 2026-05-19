package utils

import kotlinx.serialization.Serializable

@Serializable
enum class CommandName {
    INFO,
    SHOW,
    ADD,
    UPDATE,
    REMOVE_BY_ID,
    CLEAR,
    EXECUTE_SCRIPT,
    INSERT_AT,
    REMOVE_LAST,
    ADD_IF_MAX,
    AVERAGE_OF_AVERAGE_MARK,
    COUNT_LESS_THAN_AVERAGE_MARK,
    COUNT_GREATER_THAN_AVERAGE_MARK,
    UNDO,
    LOGIN,
    REGISTER,
    NODES,
    LIST_SERVERS,
    ENABLE_SERVER,
    DISABLE_SERVER,
    ADD_SERVER,
    REMOVE_SERVER
}