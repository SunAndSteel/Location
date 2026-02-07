package com.florent.location.data.repository

import androidx.room.withTransaction
import com.florent.location.data.db.AppDatabase
import com.florent.location.data.db.dao.KeyDao
import com.florent.location.data.db.dao.LeaseDao
import com.florent.location.domain.model.Key
import com.florent.location.domain.model.Lease
import com.florent.location.domain.repository.LeaseRepository

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
                keyDao.insert(key.toEntity(leaseId, lease.startDateEpochDay))
            }

            leaseId
        }
    }

    override suspend fun closeLease(leaseId: Long, endEpochDay: Long) {
        db.withTransaction {
            val updated = leaseDao.closeLease(leaseId, endEpochDay)
            require(updated == 1) { "Bail introuvable ou déjà clôturé." }
        }
    }
}
