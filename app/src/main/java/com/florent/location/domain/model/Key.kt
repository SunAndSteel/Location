package com.florent.location.domain.model

data class Key(
    val id: Long = 0L,
    val leaseId: Long = 0L,
    val type: String,
    val deviceLabel: String? = null,
    val handedOverEpochDay: Long
)
