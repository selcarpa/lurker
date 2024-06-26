package database.model

import com.benasher44.uuid.uuid4
import com.ctrip.sqllin.driver.openDatabase
import com.ctrip.sqllin.dsl.annotation.DBRow
import com.ctrip.sqllin.dsl.sql.clause.AND
import com.ctrip.sqllin.dsl.sql.clause.EQ
import com.ctrip.sqllin.dsl.sql.clause.WHERE
import database.databaseConfiguration
import database.database
import io.ktor.util.*
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import model.protocol.RecordType
import model.protocol.Resource
import utils.encodeHex

@DBRow("domain_record")
@Serializable
data class DomainRecord(
    val name: String,
    /**
     * @see model.protocol.RecordType
     */
    val recordType: Int,
    val content: String,
    val ttl: Int = 86400,
    val priority: Int = 10,
    val updateTime: Long = Clock.System.now().toEpochMilliseconds(),
    val cachedDomain: Boolean = false
) {
    val id = uuid4().toString()

    companion object {
        fun insertBatch(domainRecords: List<DomainRecord>) {
            database {
                DomainRecordTable { table ->
                    table INSERT domainRecords
                }
            }
        }

        fun replaceBatch(domainRecords: List<DomainRecord>) {
            database {
                transaction {
                    DomainRecordTable{table->

                    }
                }
            }
        }

        fun selectByNameType(name: String, recordType: RecordType): List<DomainRecord> {
            database {
                DomainRecordTable { table ->
                    return@DomainRecordTable table SELECT WHERE(
                        (this.name EQ name) AND (this.recordType EQ recordType.value.toInt())
                    )
                }
            }
            throw Exception("Database error")
        }
    }
}

fun Resource.toDomainRecord() = DomainRecord(
    name = this.rName,
    recordType = this.rType.value.toInt(),
    content = this.rData.encode().encodeHex(),
    ttl = this.ttl.toInt(),
    cachedDomain = true
)
