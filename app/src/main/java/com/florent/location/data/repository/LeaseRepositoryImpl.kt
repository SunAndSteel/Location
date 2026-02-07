package com.florent.location.data.repository

import androidx.room.withTransaction
import com.florent.location.data.db.AppDatabase
import com.florent.location.data.db.dao.KeyDao
import com.florent.location.data.db.dao.LeaseDao
import com.florent.location.domain.model.Key
import com.florent.location.domain.model.Lease
import com.florent.location.domain.repository.LeaseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LeaseRepositoryImpl(
    private val db: AppDatabase,
    private val leaseDao: LeaseDao,
    private val keyDao: KeyDao
) : LeaseRepository {
    override suspend fun createLeaseWithKeys(
        lease: Lease,
        keys: List<Key>
    ): Long {
        return db.withTransaction {
            val leaseEntity = lease.toEntity()
            val existing = leaseDao.getActiveLeaseForHousing(leaseEntity.housingId)
            require(existing == null) { "Un bail actif existe déjà pour ce logement." }

            val leaseId = leaseDao.insert(leaseEntity)

            keys.forEach { key ->
                keyDao.insert(key.toEntity(overrideLeaseId = leaseId))
            }

            leaseId
        }
    }

    override fun observeActiveLeaseForHousing(housingId: Long): Flow<Lease?> =
        leaseDao.observeActiveLeaseForHousing(housingId).map { entity ->
            entity?.toDomain()
        }

    override fun observeLease(leaseId: Long): Flow<Lease?> =
        leaseDao.observeLease(leaseId).map { entity ->
            entity?.toDomain()
        }

    override fun observeKeysForLease(leaseId: Long): Flow<List<Key>> =
        keyDao.observeKeysForLease(leaseId).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun insertKey(key: Key): Long {
        return db.withTransaction {
            val lease = leaseDao.getById(key.leaseId)
            require(lease != null) { "Bail introuvable." }
            keyDao.insert(key.toEntity())
        }
    }

    override suspend fun deleteKeyById(id: Long) {
        db.withTransaction {
            val deleted = keyDao.deleteById(id)
            require(deleted == 1) { "Clé introuvable." }
        }
    }

    override suspend fun housingExists(housingId: Long): Boolean =
        db.housingDao().exists(housingId)

    override suspend fun tenantExists(tenantId: Long): Boolean =
        db.tenantDao().exists(tenantId)

    override suspend fun closeLease(leaseId: Long, endEpochDay: Long) {
        db.withTransaction {
            val updated = leaseDao.closeLease(leaseId, endEpochDay)
            require(updated == 1) { "Bail introuvable ou déjà clôturé." }
        }
    }
}
