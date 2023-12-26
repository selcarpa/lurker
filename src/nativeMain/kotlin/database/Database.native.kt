package database

import com.ctrip.sqllin.driver.toDatabasePath

actual fun getGlobalDatabasePath(path: String) = path.toDatabasePath()
