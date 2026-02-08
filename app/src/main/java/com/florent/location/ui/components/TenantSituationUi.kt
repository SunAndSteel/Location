package com.florent.location.ui.components

import com.florent.location.domain.model.TenantSituation
import com.florent.location.domain.model.TenantStatus

fun tenantStatusLabel(status: TenantStatus): String =
    when (status) {
        TenantStatus.ACTIVE -> "Actif"
        TenantStatus.LOOKING -> "Recherche"
        TenantStatus.INACTIVE -> "Inactif"
    }

fun tenantSituationLabel(situation: TenantSituation): String {
    val base = tenantStatusLabel(situation.status)
    val leaseLabel = if (situation.hasActiveLease) "Bail actif" else "Sans bail"
    return "$base · $leaseLabel"
}
