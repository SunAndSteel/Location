package com.florent.location.domain.model

data class Tenant(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val phone: String?,
    val email: String?,
    val moveInDateEpochDay: Long?,
    val mailboxLabel: String?,
    val rentCents: Long,
    val chargesCents: Long,
    val rentDueDayOfMonth: Int
)