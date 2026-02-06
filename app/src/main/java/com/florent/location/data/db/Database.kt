package com.florent.location.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [TenantEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tenantDao(): TenantDao
}
