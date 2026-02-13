package com.florent.location.data.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigrations {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Version 2 ne modifie pas le schéma : migration no-op pour préserver les données.
        }
    }

    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            migrateServerUpdatedCursorToMillis(db, "tenants")
            migrateServerUpdatedCursorToMillis(db, "housings")
            migrateServerUpdatedCursorToMillis(db, "leases")
            migrateServerUpdatedCursorToMillis(db, "keys")
            migrateServerUpdatedCursorToMillis(db, "indexation_events")
        }

        private fun migrateServerUpdatedCursorToMillis(db: SupportSQLiteDatabase, table: String) {
            db.execSQL(
                "ALTER TABLE $table RENAME COLUMN serverUpdatedAtEpochSeconds TO serverUpdatedAtEpochMillis"
            )
            db.execSQL(
                """
                UPDATE $table
                SET serverUpdatedAtEpochMillis = CASE
                    WHEN serverUpdatedAtEpochMillis IS NULL THEN NULL
                    WHEN serverUpdatedAtEpochMillis < 100000000000 THEN serverUpdatedAtEpochMillis * 1000
                    ELSE serverUpdatedAtEpochMillis
                END
                """.trimIndent()
            )
        }
    }


    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `sync_cursors` (
                    `syncKey` TEXT NOT NULL,
                    `updatedAtEpochMillis` INTEGER NOT NULL,
                    `remoteId` TEXT NOT NULL,
                    PRIMARY KEY(`syncKey`)
                )
                """.trimIndent()
            )
        }
    }

    val ALL = arrayOf(
        MIGRATION_1_2,
        MIGRATION_2_3,
        MIGRATION_3_4,
    )
}
