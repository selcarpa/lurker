package database

import com.ctrip.sqllin.driver.DatabasePath
import com.ctrip.sqllin.dsl.Database
import database.model.Record
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

expect fun getGlobalDatabasePath(): DatabasePath

val database = Database(name = "lurker.db", path = getGlobalDatabasePath(), version = 1)


fun sample() {
    database {
        RecordTable { table ->
            table INSERT Record("content", Clock.System.now().toLocalDateTime(TimeZone.UTC))

        }
    }
}
