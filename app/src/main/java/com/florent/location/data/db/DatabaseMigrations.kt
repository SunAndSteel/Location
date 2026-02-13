package com.florent.location.data.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigrations {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Version 2 ne modifie pas le schéma : migration no-op pour préserver les données.
        }
    }

    val ALL = arrayOf(
        MIGRATION_1_2,
    )
}
