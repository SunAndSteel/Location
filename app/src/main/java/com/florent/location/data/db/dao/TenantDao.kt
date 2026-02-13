package com.florent.location.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.florent.location.data.db.entity.TenantEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TenantDao {
    @Query("SELECT * FROM tenants WHERE isDeleted = 0 ORDER BY lastName, firstName")
    fun observeAll(): Flow<List<TenantEntity>>

    @Query("SELECT * FROM tenants WHERE id = :id AND isDeleted = 0")
    fun observe(id: Long): Flow<TenantEntity?>

    @Query("SELECT * FROM tenants WHERE id = :id AND isDeleted = 0")
    fun observeById(id: Long): Flow<TenantEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tenant: TenantEntity): Long

    @Update
    suspend fun update(tenant: TenantEntity)

    @Delete
    suspend fun delete(tenant: TenantEntity)

    @Query("SELECT * FROM tenants WHERE id = :id AND isDeleted = 0")
    suspend fun getById(id: Long): TenantEntity?

    @Query("SELECT COUNT(*) > 0 FROM tenants WHERE id = :id AND isDeleted = 0")
    suspend fun exists(id: Long): Boolean

    @Query("SELECT COUNT(*) > 0 FROM leases WHERE tenantId = :id AND endDateEpochDay IS NULL AND isDeleted = 0")
    suspend fun hasActiveLease(id: Long): Boolean

    @Query("UPDATE tenants SET isDeleted = 1, dirty = 1, updatedAt = :updatedAt WHERE id = :id AND isDeleted = 0")
    suspend fun markDeletedById(id: Long, updatedAt: Long = System.currentTimeMillis()): Int

    @Query("DELETE FROM tenants WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    @Query("DELETE FROM tenants WHERE remoteId = :remoteId")
    suspend fun hardDeleteByRemoteId(remoteId: String): Int

    @Query("SELECT * FROM tenants WHERE dirty = 1")
    suspend fun getDirty(): List<TenantEntity>

    @Query("SELECT * FROM tenants WHERE remoteId = :remoteId")
    suspend fun getByRemoteId(remoteId: String): TenantEntity?

    @Query("SELECT remoteId FROM tenants")
    suspend fun getAllRemoteIds(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(tenants: List<TenantEntity>)

    @Query("UPDATE tenants SET dirty = 0, serverUpdatedAtEpochMillis = COALESCE(:serverUpdatedAtEpochMillis, serverUpdatedAtEpochMillis) WHERE remoteId = :remoteId")
    suspend fun markClean(remoteId: String, serverUpdatedAtEpochMillis: Long?)

    @Query("SELECT MAX(serverUpdatedAtEpochMillis) FROM tenants")
    suspend fun getMaxServerUpdatedAtOrNull(): Long?
}
