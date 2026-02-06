package com.florent.location.domain.usecase


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
    fun observeAll(): Flow<List<Tenant>>
    /**
     * Observe un locataire par identifiant.
     */
    fun observeById(id: Long): Flow<Tenant?>
    /**
     * Crée un locataire et renvoie l'identifiant.
     */
    suspend fun create(tenant: Tenant): Long
    /**
     * Met à jour un locataire.
     */
    suspend fun update(tenant: Tenant)
    /**
     * Supprime un locataire par identifiant.
     */
    suspend fun delete(id: Long)
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
    override fun observeAll(): Flow<List<Tenant>> =
        repository.observeAll()

    /**
     * Observe un locataire par identifiant.
     */
    override fun observeById(id: Long): Flow<Tenant?> =
        repository.observeById(id)

    /**
     * Crée un locataire.
     */
    override suspend fun create(tenant: Tenant): Long =
        repository.insert(tenant)

    /**
     * Met à jour un locataire.
     */
    override suspend fun update(tenant: Tenant) =
        repository.update(tenant)

    /**
     * Supprime un locataire.
     */
    override suspend fun delete(id: Long) =
        repository.deleteById(id)
}
