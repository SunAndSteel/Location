package com.florent.location.data.db.dao

import androidx.room.*
import com.florent.location.data.db.entity.HousingEntity
import com.florent.location.data.db.model.HousingWithActiveLease
import kotlinx.coroutines.flow.Flow

@Dao
interface HousingDao {

    // --- Liste logements (avec bail actif) ---
    // LEFT JOIN pour garder les logements libres.
    @Query(
            """
        SELECT 
            h.*,
            l.id AS lease_id,
            l.housingId AS lease_housingId,
            l.tenantId AS lease_tenantId,
            l.startDateEpochDay AS lease_startDateEpochDay,
            l.endDateEpochDay AS lease_endDateEpochDay,
            l.rentCents AS lease_rentCents,
            l.chargesCents AS lease_chargesCents,
            l.depositCents AS lease_depositCents,
            l.rentDueDayOfMonth AS lease_rentDueDayOfMonth,
            l.indexAnniversaryEpochDay AS lease_indexAnniversaryEpochDay
        FROM housings h
        LEFT JOIN leases l 
            ON l.housingId = h.id AND l.endDateEpochDay IS NULL
        ORDER BY h.city COLLATE NOCASE, h.address COLLATE NOCASE
    """
    )
    fun observeHousingsWithActiveLease(): Flow<List<HousingWithActiveLease>>

    @Query("SELECT * FROM housings ORDER BY city COLLATE NOCASE, address COLLATE NOCASE")
    fun observeHousings(): Flow<List<HousingEntity>>

    @Query("SELECT * FROM housings WHERE id = :id")
    fun observeHousing(id: Long): Flow<HousingEntity?>

    @Query("SELECT COUNT(*) > 0 FROM housings WHERE id = :id")
    suspend fun exists(id: Long): Boolean

    // --- CRUD ---
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insert(housing: HousingEntity): Long

    @Update suspend fun update(housing: HousingEntity)

    @Delete suspend fun delete(housing: HousingEntity)

    @Query("DELETE FROM housings WHERE id = :id") suspend fun deleteById(id: Long): Int
}
