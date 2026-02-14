package com.florent.location.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.florent.location.data.db.entity.SyncCursorEntity

@Dao
interface SyncCursorDao {
    @Query("SELECT * FROM sync_cursors WHERE userId = :userId AND syncKey = :syncKey")
    suspend fun getByKey(userId: String, syncKey: String): SyncCursorEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(cursor: SyncCursorEntity)
}
