package com.florent.location.domain.model



data class Lease(
    val id: Long = 0L,
    val housingId: Long,
    val tenantId: Long,
    val startDateEpochDay: Long, // LocalDate.toEpochDay()
    val endDateEpochDay: Long? = null, // null = actif
    val rentCents: Long,
    val chargesCents: Long,
    val depositCents: Long = 0L,
    val rentDueDayOfMonth: Int = 1,
    val indexAnniversaryEpochDay: Long? = null // par défaut startDateEpochDay
)
