package database

import com.ctrip.sqllin.driver.toDatabasePath

actual fun getGlobalDatabasePath()="db-test".toDatabasePath()
