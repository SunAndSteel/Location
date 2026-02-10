package com.florent.location.data.db.dao

import androidx.room.*
import com.florent.location.data.db.entity.KeyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface KeyDao {
    // Méthodes existantes
    @Query("SELECT * FROM keys WHERE housingId = :housingId ORDER BY handedOverEpochDay DESC")
    fun observeAllByHousing(housingId: Long): Flow<List<KeyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(key: KeyEntity): Long

    @Update
    suspend fun update(key: KeyEntity)

    @Delete
    suspend fun delete(key: KeyEntity)

    @Query("SELECT * FROM keys WHERE id = :id")
    suspend fun getById(id: Long): KeyEntity?

    @Query("DELETE FROM keys WHERE housingId = :housingId")
    suspend fun deleteAllByHousing(housingId: Long)

    // NOUVELLES MÉTHODES pour la synchronisation
    @Query("SELECT * FROM keys WHERE dirty = 1")
    suspend fun getDirty(): List<KeyEntity>

    @Query("SELECT * FROM keys WHERE remoteId = :remoteId")
    suspend fun getByRemoteId(remoteId: String): KeyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(keys: List<KeyEntity>)

    @Query("UPDATE keys SET dirty = 0, serverUpdatedAtEpochSeconds = :serverUpdated WHERE remoteId = :remoteId")
    suspend fun markClean(remoteId: String, serverUpdated: Long)

    @Query("SELECT MAX(serverUpdatedAtEpochSeconds) FROM keys")
    suspend fun getMaxServerUpdatedAtOrNull(): Long?
}
