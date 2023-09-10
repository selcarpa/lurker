package database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import one.tain.lurker.Database

actual class DriverFactory {
    actual fun createDriver(): SqlDriver = NativeSqliteDriver(
        Database.Schema,
        "test.db",
        maxReaderConnections = 4
    )

}
