package com.florent.location.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.florent.location.data.db.dao.HousingDao
import com.florent.location.data.db.dao.KeyDao
import com.florent.location.data.db.dao.LeaseDao
import com.florent.location.data.db.dao.TenantDao
import com.florent.location.data.db.entity.HousingEntity
import com.florent.location.data.db.entity.KeyEntity
import com.florent.location.data.db.entity.LeaseEntity
import com.florent.location.data.db.entity.TenantEntity

@Database(
        entities =
                [TenantEntity::class, HousingEntity::class, LeaseEntity::class, KeyEntity::class],
        version = 1,
        exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tenantDao(): TenantDao
    abstract fun housingDao(): HousingDao
    abstract fun leaseDao(): LeaseDao
    abstract fun keyDao(): KeyDao
}
    