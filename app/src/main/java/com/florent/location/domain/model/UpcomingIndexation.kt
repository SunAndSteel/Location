package com.florent.location.domain.model

data class UpcomingIndexation(
    val leaseId: Long,
    val housingId: Long,
    val tenantId: Long,
    val nextIndexationEpochDay: Long,
    val daysUntil: Int
)
