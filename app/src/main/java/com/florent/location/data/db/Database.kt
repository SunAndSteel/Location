package com.florent.location.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.florent.location.data.db.dao.TenantDao
import com.florent.location.data.db.entity.TenantEntity

@Database(
    entities = [TenantEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tenantDao(): TenantDao
}