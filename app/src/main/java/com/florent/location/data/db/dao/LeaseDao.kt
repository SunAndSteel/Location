package com.florent.location.data.db.dao

import androidx.room.*
import com.florent.location.data.db.entity.LeaseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LeaseDao {

    // --- Bail actif par logement ---
    @Query(
            """
        SELECT * FROM leases
        WHERE housingId = :housingId
          AND endDateEpochDay IS NULL
        LIMIT 1
    """
    )
    fun observeActiveLeaseForHousing(housingId: Long): Flow<LeaseEntity?>

    @Query(
            """
        SELECT * FROM leases
        WHERE housingId = :housingId
          AND endDateEpochDay IS NULL
        LIMIT 1
    """
    )
    suspend fun getActiveLeaseForHousing(housingId: Long): LeaseEntity?

    // --- CRUD ---
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insert(lease: LeaseEntity): Long

    @Update suspend fun update(lease: LeaseEntity)

    @Delete suspend fun delete(lease: LeaseEntity)

    @Query("SELECT * FROM leases WHERE id = :id") fun observeLease(id: Long): Flow<LeaseEntity?>

    @Query("SELECT * FROM leases WHERE id = :id") suspend fun getById(id: Long): LeaseEntity?

    // clôture simple
    @Query("UPDATE leases SET endDateEpochDay = :endEpochDay WHERE id = :leaseId")
    suspend fun closeLease(leaseId: Long, endEpochDay: Long): Int
}
