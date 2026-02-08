package com.florent.location.data.repository

import com.florent.location.data.db.dao.HousingDao
import com.florent.location.data.db.dao.KeyDao
import com.florent.location.data.db.dao.LeaseDao
import com.florent.location.domain.model.Housing
import com.florent.location.domain.model.Key
import com.florent.location.domain.repository.HousingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Implémentation de [HousingRepository] basée sur Room.
 */
class HousingRepositoryImpl(
    private val housingDao: HousingDao,
    private val keyDao: KeyDao,
    private val leaseDao: LeaseDao
) : HousingRepository {

    /**
     * Observe la liste complète des logements.
     */
    override fun observeHousings(): Flow<List<Housing>> =
        housingDao.observeHousings().map { entities ->
            entities.map { it.toDomain() }
        }

    /**
     * Observe un logement par identifiant.
     */
    override fun observeHousing(id: Long): Flow<Housing?> =
        housingDao.observeHousing(id).map { entity ->
            entity?.toDomain()
        }

    /**
     * Récupère un logement par identifiant.
     */
    override suspend fun getHousing(id: Long): Housing? =
        housingDao.getById(id)?.toDomain()

    /**
     * Insère un logement et renvoie son identifiant.
     */
    override suspend fun insert(housing: Housing): Long =
        housingDao.insert(housing.toEntity())

    /**
     * Met à jour un logement.
     */
    override suspend fun update(housing: Housing) {
        housingDao.update(housing.toEntity())
    }

    /**
     * Supprime un logement par identifiant.
     */
    override suspend fun deleteById(id: Long) {
        housingDao.deleteById(id)
    }

    override fun observeKeysForHousing(housingId: Long): Flow<List<Key>> =
        keyDao.observeKeysForHousing(housingId).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun insertKey(key: Key): Long =
        keyDao.insert(key.toEntity())

    override suspend fun deleteKeyById(id: Long) {
        val deleted = keyDao.deleteById(id)
        require(deleted == 1) { "Clé introuvable." }
    }

    /**
     * Indique si un bail actif existe pour ce logement.
     */
    override suspend fun hasActiveLease(housingId: Long): Boolean =
        leaseDao.getActiveLeaseForHousing(housingId) != null
}
