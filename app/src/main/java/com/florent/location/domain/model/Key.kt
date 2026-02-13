package com.florent.location.domain.model

data class Key(
    val id: Long = 0L,
    val housingId: Long = 0L,
    val type: String,
    val deviceLabel: String? = null,
    val handedOverEpochDay: Long,
    val remoteId: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val dirty: Boolean = true,
    val serverUpdatedAtEpochSeconds: Long? = null
)
