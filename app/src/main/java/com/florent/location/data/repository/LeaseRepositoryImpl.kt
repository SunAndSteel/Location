package com.florent.location.data.repository

import androidx.room.withTransaction
import com.florent.location.data.db.AppDatabase
import com.florent.location.data.db.dao.IndexationEventDao
import com.florent.location.data.db.dao.LeaseDao
import com.florent.location.domain.model.IndexationEvent
import com.florent.location.domain.model.Lease
import com.florent.location.domain.repository.LeaseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LeaseRepositoryImpl(
    private val db: AppDatabase,
    private val leaseDao: LeaseDao,
    private val indexationEventDao: IndexationEventDao
) : LeaseRepository {
    override suspend fun createLease(
        lease: Lease
    ): Long {
        return db.withTransaction {
            val leaseEntity = lease.toEntity()
            val existing = leaseDao.getActiveLeaseForHousing(leaseEntity.housingId)
            require(existing == null) { "Un bail actif existe déjà pour ce logement." }

            leaseDao.insert(leaseEntity)
        }
    }

    override fun observeActiveLeaseForHousing(housingId: Long): Flow<Lease?> =
        leaseDao.observeActiveLeaseForHousing(housingId).map { entity ->
            entity?.toDomain()
        }

    override fun observeActiveLeaseForTenant(tenantId: Long): Flow<Lease?> =
        leaseDao.observeActiveLeaseForTenant(tenantId).map { entity ->
            entity?.toDomain()
        }

    override fun observeActiveLeases(): Flow<List<Lease>> =
        leaseDao.observeActiveLeases().map { entities ->
            entities.map { it.toDomain() }
        }

    override fun observeLease(leaseId: Long): Flow<Lease?> =
        leaseDao.observeLease(leaseId).map { entity ->
            entity?.toDomain()
        }

    override suspend fun getLease(leaseId: Long): Lease? =
        leaseDao.getById(leaseId)?.toDomain()

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

    override fun observeIndexationEvents(leaseId: Long): Flow<List<IndexationEvent>> =
        indexationEventDao.observeEventsForLease(leaseId).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun applyIndexation(event: IndexationEvent) {
        db.withTransaction {
            val lease = leaseDao.getById(event.leaseId)
            require(lease != null) { "Bail introuvable." }
            require(lease.endDateEpochDay == null) { "Impossible d'indexer un bail clôturé." }
            require(lease.rentCents == event.baseRentCents) {
                "Le loyer a changé, veuillez relancer la simulation."
            }
            val updated = leaseDao.updateRent(event.leaseId, event.newRentCents)
            require(updated == 1) { "Échec de la mise à jour du loyer." }
            indexationEventDao.insert(event.toEntity())
        }
    }
}
