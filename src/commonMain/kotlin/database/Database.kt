package database

import com.ctrip.sqllin.driver.DatabaseConfiguration
import com.ctrip.sqllin.driver.DatabasePath
import com.ctrip.sqllin.dsl.Database

//todo load from configuration
expect fun getGlobalDatabasePath(): DatabasePath

val database by lazy {
    Database(DatabaseConfiguration(name = "lurker.db", path = getGlobalDatabasePath(), version = 1, create = {
        it.execSQL("CREATE TABLE query_record (id varchar(36) PRIMARY KEY, content TEXT, time INT, queryFrom varchar(255));")
        it.execSQL("CREATE TABLE system_operation (id varchar(36) PRIMARY KEY, type INT, time INT);")

    }, upgrade = { db, oldVersion, newVersion ->

    }))
}
