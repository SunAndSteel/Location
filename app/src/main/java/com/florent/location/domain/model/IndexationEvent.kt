package com.florent.location.domain.model

data class IndexationEvent(
    val id: Long = 0L,
    val leaseId: Long,
    val appliedEpochDay: Long,
    val baseRentCents: Long,
    val indexPercent: Double,
    val newRentCents: Long
)
