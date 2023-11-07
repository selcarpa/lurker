package database

import com.ctrip.sqllin.driver.DatabaseConfiguration
import com.ctrip.sqllin.driver.DatabasePath
import com.ctrip.sqllin.dsl.Database

//todo load from configuration
expect fun getGlobalDatabasePath(): DatabasePath
val configuration =
    DatabaseConfiguration(name = "lurker.db", path = getGlobalDatabasePath(), version = 1, create = {
        it.execSQL(
            """
CREATE TABLE query_record (id varchar(36) PRIMARY KEY, content TEXT, time INT, queryFrom varchar(255));
        """.trimIndent()
        )
        it.execSQL(
            """
CREATE TABLE system_operation (id varchar(36) PRIMARY KEY, type INT, time INT);
        """.trimIndent()
        )
        it.execSQL(
            """
create table domain_record
(
    id           varchar(36)
        primary key,
    name         varchar(255),
    recordType   INT,
    content      TEXT,
    ttl          INT,
    priority     INT,
    updateTime   INT,
    cachedDomain INT
);

create unique index domain_record_name_recordType_content_uindex
    on domain_record (name, recordType, content);
        """.trimIndent()
        )
    }, upgrade = { db, oldVersion, newVersion ->

    })

val database = Database(configuration)
