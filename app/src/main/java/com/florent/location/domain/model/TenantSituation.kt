package com.florent.location.domain.model

/**
 * Projection de situation d'un locataire.
 */
data class TenantSituation(
    val status: TenantStatus,
    val hasActiveLease: Boolean
)
