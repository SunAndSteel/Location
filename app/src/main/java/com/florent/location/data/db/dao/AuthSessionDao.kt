package com.florent.location.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.florent.location.data.db.entity.AuthSessionEntity

@Dao
interface AuthSessionDao {
    @Query("SELECT * FROM auth_session WHERE id = 1 LIMIT 1")
    suspend fun getOrNull(): AuthSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: AuthSessionEntity)

    @Query("DELETE FROM auth_session")
    suspend fun clear()
}
