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
    val type: Int, val time: LocalDateTime
) {
    val id = uuid4().toString()

    companion object {
        fun insert(systemOperation: SystemOperation) {
            database {
                SystemOperationTable { table ->
                    table INSERT systemOperation
                }
            }
        }

        fun insert(systemOperationType: SystemOperationType) {
            insert(SystemOperation(systemOperationType.value,  Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())))
        }
    }
}

enum class SystemOperationType(val value: Int) {
    STARTUP(0),
    SHUTDOWN(1),
}


