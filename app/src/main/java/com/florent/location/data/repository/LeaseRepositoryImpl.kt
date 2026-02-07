package com.florent.location.data.repository

import androidx.room.withTransaction
import com.florent.location.data.db.AppDatabase
import com.florent.location.data.db.entity.KeyEntity
import com.florent.location.data.db.entity.LeaseEntity
import com.florent.location.domain.model.Lease
import com.florent.location.domain.repository.LeaseRepository

class LeaseRepositoryImpl(
    private val db: AppDatabase
) : LeaseRepository {
    private val leaseDao = db.leaseDao()
    private val keyDao = db.keyDao()

    override suspend fun createLeaseWithKeys(
        lease: LeaseEntity,
        keys: List<KeyEntity>
    ): Long {
        return db.withTransaction {
            val existing = leaseDao.getActiveLeaseForHousing(lease.housingId)
            require(existing == null) { "Un bail actif existe déjà pour ce logement." }

            val leaseId = leaseDao.insert(lease)

            keys.forEach { key ->
                keyDao.insert(key.copy(leaseId = leaseId))
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
