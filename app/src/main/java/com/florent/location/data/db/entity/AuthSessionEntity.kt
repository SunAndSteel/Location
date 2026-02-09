package com.florent.location.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "auth_session")
data class AuthSessionEntity(
    @PrimaryKey val id: Int = 1, // singleton
    val accessToken: String,
    val refreshToken: String,
    val userId: String,
    val email: String?,
    val expiresAtEpochSeconds: Long
)