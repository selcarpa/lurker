package database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import database.Database


actual class DriverFactory {
    actual fun createDriver(): SqlDriver {
        val jdbcSqliteDriver = JdbcSqliteDriver("jdbc:sqlite:test.db")
        return jdbcSqliteDriver.also {
            Database.Schema.create(jdbcSqliteDriver)
        }
    }

}
