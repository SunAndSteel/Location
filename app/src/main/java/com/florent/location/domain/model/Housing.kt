package com.florent.location.domain.model

/**
 * Modèle de domaine pour un logement.
 */
data class Housing(
    val id: Long = 0L,
    val city: String,
    val address: String,
    val defaultRentCents: Long = 0L,
    val defaultChargesCents: Long = 0L,
    val depositCents: Long = 0L,
    val peb: String? = null,
    val buildingLabel: String? = null
)
