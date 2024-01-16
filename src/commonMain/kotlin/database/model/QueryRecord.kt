package database.model

import com.benasher44.uuid.uuid4
import com.ctrip.sqllin.dsl.annotation.DBRow
import database.database
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import model.protocol.Question

@DBRow("query_record")
@Serializable
data class QueryRecord(
    val content: String,
    val time: Long,
    val queryFrom: String
) {
    val id = uuid4().toString()
    lateinit var unionId: String

    companion object {
        fun insert(queryRecord: QueryRecord) {
            insertBatch(listOf(queryRecord))
        }

        fun insertBatch(queryRecords: Iterable<QueryRecord>) {
            val unionId = uuid4().toString()
            queryRecords.forEach {
                it.unionId = unionId
            }
            database {
                QueryRecordTable { table ->
                    table INSERT queryRecords
                }
            }
        }
    }
}

fun Question.toQueryRecord(queryFrom: String): QueryRecord {
    return QueryRecord(this.toString(), Clock.System.now().epochSeconds, queryFrom)
}

