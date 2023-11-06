package database.model

import com.benasher44.uuid.uuid4
import com.ctrip.sqllin.dsl.annotation.DBRow
import database.database
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@DBRow("query_record")
@Serializable
data class QueryRecord(
    val content: String, val time: LocalDateTime
) {
    val id = uuid4().toString()

    companion object {
        fun insert(queryRecord: QueryRecord) {
            database {
                QueryRecordTable { table ->
                    table INSERT queryRecord
                }
            }
        }
    }
}

