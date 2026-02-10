package com.florent.location.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.florent.location.data.db.entity.IndexationEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IndexationEventDao {
    // Méthodes existantes
    @Query("SELECT * FROM indexation_events WHERE leaseId = :leaseId ORDER BY appliedEpochDay DESC")
    fun observeAllByLease(leaseId: Long): Flow<List<IndexationEventEntity>>

    @Query("SELECT * FROM indexation_events WHERE leaseId = :leaseId ORDER BY appliedEpochDay DESC")
    fun observeEventsForLease(leaseId: Long): Flow<List<IndexationEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: IndexationEventEntity): Long

    @Update
    suspend fun update(event: IndexationEventEntity)

    @Delete
    suspend fun delete(event: IndexationEventEntity)

    @Query("SELECT * FROM indexation_events WHERE id = :id")
    suspend fun getById(id: Long): IndexationEventEntity?

    @Query("SELECT * FROM indexation_events WHERE leaseId = :leaseId ORDER BY appliedEpochDay DESC LIMIT 1")
    suspend fun getLatestByLease(leaseId: Long): IndexationEventEntity?

    // NOUVELLES MÉTHODES pour la synchronisation
    @Query("SELECT * FROM indexation_events WHERE dirty = 1")
    suspend fun getDirty(): List<IndexationEventEntity>

    @Query("SELECT * FROM indexation_events WHERE remoteId = :remoteId")
    suspend fun getByRemoteId(remoteId: String): IndexationEventEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(events: List<IndexationEventEntity>)

    @Query("UPDATE indexation_events SET dirty = 0, serverUpdatedAtEpochSeconds = :serverUpdated WHERE remoteId = :remoteId")
    suspend fun markClean(remoteId: String, serverUpdated: Long)

    @Query("SELECT MAX(serverUpdatedAtEpochSeconds) FROM indexation_events")
    suspend fun getMaxServerUpdatedAtOrNull(): Long?
}