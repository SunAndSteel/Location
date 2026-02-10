package com.florent.location.data.db.dao

import androidx.room.*
import com.florent.location.data.db.entity.LeaseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LeaseDao {
    // Méthodes existantes
    @Query("SELECT * FROM leases WHERE id = :id")
    fun observe(id: Long): Flow<LeaseEntity?>

    @Query("SELECT * FROM leases WHERE housingId = :housingId AND endDateEpochDay IS NULL")
    fun observeActiveByHousing(housingId: Long): Flow<LeaseEntity?>

    @Query("SELECT * FROM leases WHERE tenantId = :tenantId AND endDateEpochDay IS NULL")
    fun observeActiveByTenant(tenantId: Long): Flow<LeaseEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(lease: LeaseEntity): Long

    @Update
    suspend fun update(lease: LeaseEntity)

    @Delete
    suspend fun delete(lease: LeaseEntity)

    @Query("SELECT * FROM leases WHERE id = :id")
    suspend fun getById(id: Long): LeaseEntity?

    @Query("SELECT * FROM leases WHERE housingId = :housingId AND endDateEpochDay IS NULL")
    suspend fun getActiveByHousing(housingId: Long): LeaseEntity?

    @Query("SELECT * FROM leases WHERE tenantId = :tenantId AND endDateEpochDay IS NULL")
    suspend fun getActiveByTenant(tenantId: Long): LeaseEntity?

    @Query("SELECT * FROM leases WHERE housingId = :housingId ORDER BY startDateEpochDay DESC")
    fun observeAllByHousing(housingId: Long): Flow<List<LeaseEntity>>

    @Query("SELECT * FROM leases WHERE tenantId = :tenantId ORDER BY startDateEpochDay DESC")
    fun observeAllByTenant(tenantId: Long): Flow<List<LeaseEntity>>

    // NOUVELLES MÉTHODES pour la synchronisation
    @Query("SELECT * FROM leases WHERE dirty = 1")
    suspend fun getDirty(): List<LeaseEntity>

    @Query("SELECT * FROM leases WHERE remoteId = :remoteId")
    suspend fun getByRemoteId(remoteId: String): LeaseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(leases: List<LeaseEntity>)

    @Query("UPDATE leases SET dirty = 0, serverUpdatedAtEpochSeconds = :serverUpdated WHERE remoteId = :remoteId")
    suspend fun markClean(remoteId: String, serverUpdated: Long)

    @Query("SELECT MAX(serverUpdatedAtEpochSeconds) FROM leases")
    suspend fun getMaxServerUpdatedAtOrNull(): Long?
}

