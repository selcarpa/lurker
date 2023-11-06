package database

import com.ctrip.sqllin.driver.DatabaseConfiguration
import com.ctrip.sqllin.driver.DatabasePath
import com.ctrip.sqllin.dsl.Database

expect fun getGlobalDatabasePath(): DatabasePath


val database by lazy {
    Database(DatabaseConfiguration(name = "lurker.db", path = getGlobalDatabasePath(), version = 1, create = {
        it.execSQL("CREATE TABLE Record (id varchar(36) PRIMARY KEY, content TEXT, time INT);")

    }, upgrade = { _, _, _ ->
        //ignored
    }))
}
