package com.florent.location.domain.model

/**
 * Modèle de domaine représentant un locataire.
 */
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
