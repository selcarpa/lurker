package database.model

import com.benasher44.uuid.uuid4
import com.ctrip.sqllin.dsl.annotation.DBRow
import database.database
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@DBRow
@Serializable
data class Record(
    val content: String, val time: LocalDateTime
) {
    val id = uuid4().toString()
}

object QueryRecord {
    fun insert(record: Record) {
        database {
            RecordTable { table ->
                table INSERT record
            }
        }
    }

}
