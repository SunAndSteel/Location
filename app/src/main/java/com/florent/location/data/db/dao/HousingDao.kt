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
            l.indexAnniversaryEpochDay AS lease_indexAnniversaryEpochDay,
            l.rentOverridden AS lease_rentOverridden,
            l.chargesOverridden AS lease_chargesOverridden,
            l.depositOverridden AS lease_depositOverridden,
            l.housingRentCentsSnapshot AS lease_housingRentCentsSnapshot,
            l.housingChargesCentsSnapshot AS lease_housingChargesCentsSnapshot,
            l.housingDepositCentsSnapshot AS lease_housingDepositCentsSnapshot
        FROM housings h
        LEFT JOIN leases l 
            ON l.housingId = h.id AND l.endDateEpochDay IS NULL
        ORDER BY h.addr_city COLLATE NOCASE, h.addr_street COLLATE NOCASE, h.addr_number COLLATE NOCASE
    """
    )
    fun observeHousingsWithActiveLease(): Flow<List<HousingWithActiveLease>>

    @Query(
        "SELECT * FROM housings ORDER BY addr_city COLLATE NOCASE, addr_street COLLATE NOCASE, addr_number COLLATE NOCASE"
    )
    fun observeHousings(): Flow<List<HousingEntity>>

    @Query("SELECT * FROM housings WHERE id = :id")
    fun observeHousing(id: Long): Flow<HousingEntity?>

    @Query("SELECT * FROM housings WHERE id = :id")
    suspend fun getById(id: Long): HousingEntity?

    @Query("SELECT COUNT(*) > 0 FROM housings WHERE id = :id")
    suspend fun exists(id: Long): Boolean

    // --- CRUD ---
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insert(housing: HousingEntity): Long

    @Update suspend fun update(housing: HousingEntity)

    @Delete suspend fun delete(housing: HousingEntity)

    @Query("DELETE FROM housings WHERE id = :id") suspend fun deleteById(id: Long): Int

    @Query("SELECT * FROM housings WHERE dirty = 1")
    suspend fun getDirty(): List<HousingEntity>

    @Query("SELECT * FROM housings WHERE remoteId = :remoteId LIMIT 1")
    suspend fun getByRemoteId(remoteId: String): HousingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<HousingEntity>)

    @Query("UPDATE housings SET dirty = 0, serverUpdatedAtEpochSeconds = :serverUpdatedAt WHERE remoteId = :remoteId")
    suspend fun markClean(remoteId: String, serverUpdatedAt: Long)

    @Query("SELECT serverUpdatedAtEpochSeconds FROM housings ORDER BY serverUpdatedAtEpochSeconds DESC LIMIT 1")
    suspend fun getMaxServerUpdatedAtOrNull(): Long?

    @Query("UPDATE housings SET dirty = 1 WHERE id = :id")
    suspend fun markDirtyById(id: Long)

    @Query("UPDATE housings SET dirty = 1 WHERE remoteId = :remoteId")
    suspend fun markDirtyByRemoteId(remoteId: String)
}
