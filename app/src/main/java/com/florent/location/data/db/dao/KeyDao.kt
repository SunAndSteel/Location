package com.florent.location.data.db.dao

import androidx.room.*
import com.florent.location.data.db.entity.KeyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface KeyDao {

    // --- Clés par logement ---
    @Query(
            """
        SELECT * FROM keys
        WHERE housingId = :housingId
        ORDER BY handedOverEpochDay DESC, id DESC
    """
    )
    fun observeKeysForHousing(housingId: Long): Flow<List<KeyEntity>>

    // --- CRUD ---
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insert(key: KeyEntity): Long

    @Update suspend fun update(key: KeyEntity)

    @Delete suspend fun delete(key: KeyEntity)

    @Query("DELETE FROM keys WHERE id = :id") suspend fun deleteById(id: Long): Int
}
