package database

import com.ctrip.sqllin.driver.DatabaseConfiguration
import com.ctrip.sqllin.driver.DatabasePath
import com.ctrip.sqllin.dsl.Database
import model.config.Config.Configuration

expect fun getGlobalDatabasePath(path: String): DatabasePath
val databaseConfiguration = DatabaseConfiguration(name = Configuration.database.name,
    path = getGlobalDatabasePath(Configuration.database.path),
    version = 3,
    create = {
        it.execSQL(
            """
CREATE TABLE query_record (id VARCHAR(36) PRIMARY KEY, content TEXT, time INT, queryFrom VARCHAR(255), unionId VARCHAR(36));
        """.trimIndent()
        )
        it.execSQL(
            """
CREATE INDEX query_record_unionId_index ON query_record (unionId);
            """.trimIndent()
        )
        it.execSQL(
            """
CREATE TABLE system_operation (id VARCHAR(36) PRIMARY KEY, type INT, time INT, content TEXT);
        """.trimIndent()
        )
        it.execSQL(
            """
create table domain_record
(
    id           varchar(36) primary key,
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
    },
    upgrade = { db, oldVersion, newVersion ->
        if (oldVersion < 2) {
            db.execSQL(
                """
ALTER TABLE system_operation ADD COLUMN content TEXT;
                    """.trimIndent()
            )
        }
        if (oldVersion < 3) {
            db.execSQL(
                """
ALTER TABLE query_record ADD COLUMN unionId VARCHAR(36);
                """.trimIndent()
            )
            db.execSQL(
                """
CREATE INDEX query_record_unionId_index ON query_record (unionId);
            """.trimIndent()
            )
        }
    })

val database = Database(databaseConfiguration, Configuration.debug)
