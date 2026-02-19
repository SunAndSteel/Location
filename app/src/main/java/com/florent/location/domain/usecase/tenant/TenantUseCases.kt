package com.florent.location.domain.usecase.tenant

import com.florent.location.domain.model.Tenant
import com.florent.location.domain.repository.TenantRepository
import kotlinx.coroutines.flow.Flow

/**
 * Cas d'usage disponibles pour la gestion des locataires.
 */
interface TenantUseCases {
    /**
     * Observe l'ensemble des locataires.
     */
    fun observeTenants(): Flow<List<Tenant>>

    /**
     * Observe un locataire par identifiant.
     */
    fun observeTenant(id: Long): Flow<Tenant?>

    /**
     * Crée un locataire et renvoie l'identifiant.
     */
    suspend fun createTenant(tenant: Tenant): Long

    /**
     * Met à jour un locataire.
     */
    suspend fun updateTenant(tenant: Tenant)

    /**
     * Supprime un locataire par identifiant.
     */
    suspend fun deleteTenant(id: Long)
}

/**
 * Implémentation des cas d'usage basés sur le [TenantRepository].
 */
class TenantUseCasesImpl(
    private val repository: TenantRepository
) : TenantUseCases {

    /**
     * Observe tous les locataires.
     */
    override fun observeTenants(): Flow<List<Tenant>> =
        repository.observeTenants()

    /**
     * Observe un locataire par identifiant.
     */
    override fun observeTenant(id: Long): Flow<Tenant?> =
        repository.observeTenant(id)

    /**
     * Crée un locataire.
     */
    override suspend fun createTenant(tenant: Tenant): Long {
        validateTenant(tenant)
        return repository.createTenant(tenant)
    }

    /**
     * Met à jour un locataire.
     */
    override suspend fun updateTenant(tenant: Tenant) {
        validateTenant(tenant)
        repository.updateTenant(tenant)
    }

    /**
     * Supprime un locataire.
     */
    override suspend fun deleteTenant(id: Long) =
        repository.deleteTenant(id)

    private fun validateTenant(tenant: Tenant) {
        require(tenant.firstName.isNotBlank()) { "Le prénom est obligatoire." }
        require(tenant.lastName.isNotBlank()) { "Le nom est obligatoire." }
    }
}
