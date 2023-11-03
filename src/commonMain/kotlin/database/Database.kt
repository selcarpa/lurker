package database

import com.ctrip.sqllin.driver.DatabasePath
import com.ctrip.sqllin.dsl.Database

expect fun getGlobalDatabasePath(): DatabasePath

val database = Database(name = "lurker.db", path = getGlobalDatabasePath(), version = 1)
