package com.florent.location.data.repository

import com.florent.location.data.db.dao.TenantDao
import com.florent.location.domain.model.Tenant
import com.florent.location.domain.repository.TenantRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Implémentation de [TenantRepository] basée sur Room.
 */
class TenantRepositoryImpl (
    private val dao: TenantDao
) : TenantRepository {
    /**
     * Observe la liste complète des locataires.
     */
    override fun observeTenants(): Flow<List<Tenant>> =
        dao.observeAll().map { entities ->
            entities.map { it.toDomain() }
        }

    /**
     * Observe un locataire par son identifiant.
     */
    override fun observeTenant(id: Long): Flow<Tenant?> =
        dao.observeById(id).map { entity ->
            entity?.toDomain()
        }

    /**
     * Insère un locataire et renvoie son identifiant.
     */
    override suspend fun createTenant(tenant: Tenant): Long =
        dao.insert(tenant.toEntity())

    /**
     * Met à jour un locataire existant.
     */
    override suspend fun updateTenant(tenant: Tenant) =
        dao.update(tenant.toEntity())

    /**
     * Supprime un locataire par identifiant.
     */
    override suspend fun deleteTenant(id: Long) {
        val hasActiveLease = dao.hasActiveLease(id)
        require(!hasActiveLease) { "Impossible de supprimer un locataire avec un bail actif." }
        dao.deleteById(id)
    }
}
