package com.florent.location.data.db.dao

import androidx.room.*
import com.florent.location.data.db.entity.KeyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface KeyDao {

    // --- Clés par bail ---
    @Query(
            """
        SELECT * FROM keys
        WHERE leaseId = :leaseId
        ORDER BY handedOverEpochDay DESC, id DESC
    """
    )
    fun observeKeysForLease(leaseId: Long): Flow<List<KeyEntity>>

    // --- CRUD ---
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insert(key: KeyEntity): Long

    @Update suspend fun update(key: KeyEntity)

    @Delete suspend fun delete(key: KeyEntity)

    @Query("DELETE FROM keys WHERE id = :id") suspend fun deleteById(id: Long): Int
}
