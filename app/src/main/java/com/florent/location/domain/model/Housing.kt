package com.florent.location.domain.model

/**
 * Modèle de domaine pour un logement.
 */
data class Housing(
    val id: Long = 0L,
    val remoteId: String,
    val address: Address,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isArchived: Boolean = false,
    val rentCents: Long = 0L,
    val chargesCents: Long = 0L,
    val depositCents: Long = 0L,
    val meterGasId: String? = null,
    val meterElectricityId: String? = null,
    val meterWaterId: String? = null,
    val mailboxLabel: String? = null,
    val pebRating: PebRating = PebRating.UNKNOWN,
    val pebDate: String? = null,
    val buildingLabel: String? = null,
    val internalNote: String? = null
)
