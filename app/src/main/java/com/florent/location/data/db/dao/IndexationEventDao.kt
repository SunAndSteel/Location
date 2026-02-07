package com.florent.location.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.florent.location.data.db.entity.IndexationEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IndexationEventDao {
    @Query(
        """
        SELECT * FROM indexation_events
        WHERE leaseId = :leaseId
        ORDER BY appliedEpochDay DESC
        """
    )
    fun observeEventsForLease(leaseId: Long): Flow<List<IndexationEventEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(event: IndexationEventEntity): Long
}
