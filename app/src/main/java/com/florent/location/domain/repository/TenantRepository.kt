package com.florent.location.domain.repository

import com.florent.location.domain.model.Tenant
import kotlinx.coroutines.flow.Flow

/**
 * Contrat de persistence pour les locataires.
 */
interface TenantRepository {
    /**
     * Observe la liste complète des locataires.
     */
    fun observeTenants(): Flow<List<Tenant>>
    /**
     * Observe un locataire par identifiant.
     */
    fun observeTenant(id: Long): Flow<Tenant?>
    /**
     * Insère un locataire et renvoie l'identifiant.
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
