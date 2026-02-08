package com.florent.location.domain.usecase.tenant

import com.florent.location.domain.model.Tenant
import com.florent.location.domain.model.TenantSituation
import com.florent.location.domain.repository.LeaseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Observe la situation d'un locataire (statut + bail actif).
 */
class ObserveTenantSituation(
    private val leaseRepository: LeaseRepository
) {
    operator fun invoke(tenant: Tenant): Flow<TenantSituation> =
        leaseRepository.observeActiveLeaseForTenant(tenant.id).map { lease ->
            TenantSituation(
                status = tenant.status,
                hasActiveLease = lease != null
            )
        }
}
