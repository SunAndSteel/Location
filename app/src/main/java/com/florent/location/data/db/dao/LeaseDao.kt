package com.florent.location.data.db.dao

import androidx.room.*
import com.florent.location.data.db.entity.LeaseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LeaseDao {
    @Query("SELECT * FROM leases WHERE id = :id AND isDeleted = 0")
    fun observe(id: Long): Flow<LeaseEntity?>

    @Query("SELECT * FROM leases WHERE id = :id AND isDeleted = 0")
    fun observeLease(id: Long): Flow<LeaseEntity?>

    @Query("SELECT * FROM leases WHERE housingId = :housingId AND endDateEpochDay IS NULL AND isDeleted = 0")
    fun observeActiveByHousing(housingId: Long): Flow<LeaseEntity?>

    @Query("SELECT * FROM leases WHERE housingId = :housingId AND endDateEpochDay IS NULL AND isDeleted = 0")
    fun observeActiveLeaseForHousing(housingId: Long): Flow<LeaseEntity?>

    @Query("SELECT * FROM leases WHERE tenantId = :tenantId AND endDateEpochDay IS NULL AND isDeleted = 0")
    fun observeActiveByTenant(tenantId: Long): Flow<LeaseEntity?>

    @Query("SELECT * FROM leases WHERE tenantId = :tenantId AND endDateEpochDay IS NULL AND isDeleted = 0")
    fun observeActiveLeaseForTenant(tenantId: Long): Flow<LeaseEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(lease: LeaseEntity): Long

    @Update
    suspend fun update(lease: LeaseEntity)

    @Delete
    suspend fun delete(lease: LeaseEntity)

    @Query("SELECT * FROM leases WHERE id = :id AND isDeleted = 0")
    suspend fun getById(id: Long): LeaseEntity?

    @Query("SELECT * FROM leases WHERE housingId = :housingId AND endDateEpochDay IS NULL AND isDeleted = 0")
    suspend fun getActiveByHousing(housingId: Long): LeaseEntity?

    @Query("SELECT * FROM leases WHERE housingId = :housingId AND endDateEpochDay IS NULL AND isDeleted = 0")
    suspend fun getActiveLeaseForHousing(housingId: Long): LeaseEntity?

    @Query("SELECT * FROM leases WHERE tenantId = :tenantId AND endDateEpochDay IS NULL AND isDeleted = 0")
    suspend fun getActiveByTenant(tenantId: Long): LeaseEntity?

    @Query("SELECT * FROM leases WHERE housingId = :housingId AND isDeleted = 0 ORDER BY startDateEpochDay DESC")
    fun observeAllByHousing(housingId: Long): Flow<List<LeaseEntity>>

    @Query("SELECT * FROM leases WHERE tenantId = :tenantId AND isDeleted = 0 ORDER BY startDateEpochDay DESC")
    fun observeAllByTenant(tenantId: Long): Flow<List<LeaseEntity>>

    @Query("SELECT * FROM leases WHERE endDateEpochDay IS NULL AND isDeleted = 0 ORDER BY startDateEpochDay DESC")
    fun observeActiveLeases(): Flow<List<LeaseEntity>>

    @Query("SELECT * FROM leases WHERE dirty = 1")
    suspend fun getDirty(): List<LeaseEntity>

    @Query("SELECT * FROM leases WHERE remoteId = :remoteId")
    suspend fun getByRemoteId(remoteId: String): LeaseEntity?

    @Query("SELECT remoteId FROM leases")
    suspend fun getAllRemoteIds(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(leases: List<LeaseEntity>)

    @Query("UPDATE leases SET dirty = 0, serverUpdatedAtEpochSeconds = COALESCE(:serverUpdated, serverUpdatedAtEpochSeconds) WHERE remoteId = :remoteId")
    suspend fun markClean(remoteId: String, serverUpdated: Long?)

    @Query("SELECT MAX(serverUpdatedAtEpochSeconds) FROM leases")
    suspend fun getMaxServerUpdatedAtOrNull(): Long?

    @Query("UPDATE leases SET endDateEpochDay = :endEpochDay, updatedAt = :updatedAt, dirty = 1 WHERE id = :leaseId AND endDateEpochDay IS NULL AND isDeleted = 0")
    suspend fun closeLease(leaseId: Long, endEpochDay: Long, updatedAt: Long = System.currentTimeMillis()): Int

    @Query("UPDATE leases SET rentCents = :newRentCents, updatedAt = :updatedAt, dirty = 1 WHERE id = :leaseId AND isDeleted = 0")
    suspend fun updateRent(leaseId: Long, newRentCents: Long, updatedAt: Long = System.currentTimeMillis()): Int

    @Query("UPDATE leases SET isDeleted = 1, dirty = 1, updatedAt = :updatedAt WHERE id = :id AND isDeleted = 0")
    suspend fun markDeletedById(id: Long, updatedAt: Long = System.currentTimeMillis()): Int

    @Query("DELETE FROM leases WHERE remoteId = :remoteId")
    suspend fun hardDeleteByRemoteId(remoteId: String): Int
}
