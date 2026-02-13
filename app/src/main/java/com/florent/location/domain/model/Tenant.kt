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
    val status: TenantStatus = TenantStatus.ACTIVE,
    val remoteId: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val dirty: Boolean = true,
    val serverUpdatedAtEpochSeconds: Long? = null
)
