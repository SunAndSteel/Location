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
    fun observeAll(): Flow<List<Tenant>>
    /**
     * Observe un locataire par identifiant.
     */
    fun observeById(id: Long): Flow<Tenant?>
    /**
     * Insère un locataire et renvoie l'identifiant.
     */
    suspend fun insert(tenant: Tenant): Long
    /**
     * Met à jour un locataire.
     */
    suspend fun update(tenant: Tenant)
    /**
     * Supprime un locataire par identifiant.
     */
    suspend fun deleteById(id: Long)
}
