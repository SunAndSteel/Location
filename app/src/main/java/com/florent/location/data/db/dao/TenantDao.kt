package com.florent.location.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.florent.location.data.db.entity.TenantEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO Room pour manipuler les locataires.
 */
@Dao
interface TenantDao {
    // Méthodes existantes
    @Query("SELECT * FROM tenants ORDER BY lastName, firstName")
    fun observeAll(): Flow<List<TenantEntity>>

    @Query("SELECT * FROM tenants WHERE id = :id")
    fun observe(id: Long): Flow<TenantEntity?>

    @Query("SELECT * FROM tenants WHERE id = :id")
    fun observeById(id: Long): Flow<TenantEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tenant: TenantEntity): Long

    @Update
    suspend fun update(tenant: TenantEntity)

    @Delete
    suspend fun delete(tenant: TenantEntity)

    @Query("SELECT * FROM tenants WHERE id = :id")
    suspend fun getById(id: Long): TenantEntity?

    @Query("SELECT COUNT(*) > 0 FROM tenants WHERE id = :id")
    suspend fun exists(id: Long): Boolean

    @Query("SELECT COUNT(*) > 0 FROM leases WHERE tenantId = :id AND endDateEpochDay IS NULL")
    suspend fun hasActiveLease(id: Long): Boolean

    @Query("DELETE FROM tenants WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    // NOUVELLES MÉTHODES pour la synchronisation
    @Query("SELECT * FROM tenants WHERE dirty = 1")
    suspend fun getDirty(): List<TenantEntity>

    @Query("SELECT * FROM tenants WHERE remoteId = :remoteId")
    suspend fun getByRemoteId(remoteId: String): TenantEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(tenants: List<TenantEntity>)

    @Query("UPDATE tenants SET dirty = 0, serverUpdatedAtEpochSeconds = :serverUpdated WHERE remoteId = :remoteId")
    suspend fun markClean(remoteId: String, serverUpdated: Long)

    @Query("SELECT MAX(serverUpdatedAtEpochSeconds) FROM tenants")
    suspend fun getMaxServerUpdatedAtOrNull(): Long?
}
