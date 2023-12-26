package database.model

import com.benasher44.uuid.uuid4
import com.ctrip.sqllin.dsl.annotation.DBRow
import database.database
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable

@DBRow("system_operation")
@Serializable
data class SystemOperation(
    val type: Int, val time: Long, val content: String?
) {
    val id = uuid4().toString()

    constructor(type: Int, time: Long) : this(type, time, null)

    companion object {
        fun insert(systemOperation: SystemOperation) {
            database {
                SystemOperationTable { table ->
                    table INSERT systemOperation
                }
            }
        }

        fun insert(systemOperationType: SystemOperationType) {
            insert(SystemOperation(systemOperationType.value, Clock.System.now().epochSeconds))
        }
    }
}

enum class SystemOperationType(val value: Int, val template: String?) {
    STARTUP(0, null),
    SHUTDOWN(1, null),
    MODIFY_CONFIG(2, "Modify config: %s");

    companion object {
        fun fromValue(value: Int): SystemOperationType? {
            return entries.find { it.value == value }
        }
    }
}


