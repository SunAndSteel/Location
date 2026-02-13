package com.florent.location.data.db.dao

import androidx.room.*
import com.florent.location.data.db.entity.KeyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface KeyDao {
    @Query("SELECT * FROM keys WHERE housingId = :housingId AND isDeleted = 0 ORDER BY handedOverEpochDay DESC")
    fun observeAllByHousing(housingId: Long): Flow<List<KeyEntity>>

    @Query("SELECT * FROM keys WHERE housingId = :housingId AND isDeleted = 0 ORDER BY handedOverEpochDay DESC")
    fun observeKeysForHousing(housingId: Long): Flow<List<KeyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(key: KeyEntity): Long

    @Update
    suspend fun update(key: KeyEntity)

    @Delete
    suspend fun delete(key: KeyEntity)

    @Query("SELECT * FROM keys WHERE id = :id AND isDeleted = 0")
    suspend fun getById(id: Long): KeyEntity?

    @Query("DELETE FROM keys WHERE housingId = :housingId")
    suspend fun deleteAllByHousing(housingId: Long)

    @Query("UPDATE keys SET isDeleted = 1, dirty = 1, updatedAt = :updatedAt WHERE id = :id AND isDeleted = 0")
    suspend fun markDeletedById(id: Long, updatedAt: Long = System.currentTimeMillis()): Int

    @Query("DELETE FROM keys WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    @Query("DELETE FROM keys WHERE remoteId = :remoteId")
    suspend fun hardDeleteByRemoteId(remoteId: String): Int

    @Query("SELECT * FROM keys WHERE dirty = 1")
    suspend fun getDirty(): List<KeyEntity>

    @Query("SELECT * FROM keys WHERE remoteId = :remoteId")
    suspend fun getByRemoteId(remoteId: String): KeyEntity?

    @Query("SELECT remoteId FROM keys")
    suspend fun getAllRemoteIds(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(keys: List<KeyEntity>)

    @Query("UPDATE keys SET dirty = 0, serverUpdatedAtEpochSeconds = COALESCE(:serverUpdated, serverUpdatedAtEpochSeconds) WHERE remoteId = :remoteId")
    suspend fun markClean(remoteId: String, serverUpdated: Long?)

    @Query("SELECT MAX(serverUpdatedAtEpochSeconds) FROM keys")
    suspend fun getMaxServerUpdatedAtOrNull(): Long?
}
