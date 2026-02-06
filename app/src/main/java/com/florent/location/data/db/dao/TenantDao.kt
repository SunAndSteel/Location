package com.florent.location.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.florent.location.data.db.entity.TenantEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TenantDao {

    @Query("SELECT * FROM tenants ORDER BY lastName ASC, firstName ASC")
    fun observeAll(): Flow<List<TenantEntity>>

    @Query("SELECT * FROM tenants WHERE id = :id LIMIT 1")
    fun observeById(id: Long): Flow<TenantEntity?>

    @Insert(onConflict = OnConflictStrategy.Companion.ABORT)
    suspend fun insert(entity: TenantEntity): Long

    @Update
    suspend fun update(entity: TenantEntity)

    @Query("DELETE FROM tenants WHERE id = :id")
    suspend fun deleteById(id: Long)
}