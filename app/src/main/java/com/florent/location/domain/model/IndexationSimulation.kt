package com.florent.location.domain.model

data class IndexationSimulation(
    val leaseId: Long,
    val baseRentCents: Long,
    val indexPercent: Double,
    val newRentCents: Long,
    val effectiveEpochDay: Long
)
