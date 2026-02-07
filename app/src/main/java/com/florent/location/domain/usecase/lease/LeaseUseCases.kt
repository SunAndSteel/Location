package com.florent.location.domain.usecase.lease

import com.florent.location.domain.model.Key
import com.florent.location.domain.model.Lease
import com.florent.location.domain.repository.LeaseRepository

/**
 * Cas d'usage disponibles pour la gestion des baux.
 */
interface LeaseUseCases {
    /**
     * Crée un bail avec ses clés associées.
     */
    suspend fun createLeaseWithKeys(lease: Lease, keys: List<Key>): Long

    /**
     * Clôture un bail existant.
     */
    suspend fun closeLease(leaseId: Long, endEpochDay: Long)
}

/**
 * Implémentation des cas d'usage basés sur le [LeaseRepository].
 */
class LeaseUseCasesImpl(
    private val repository: LeaseRepository
) : LeaseUseCases {

    override suspend fun createLeaseWithKeys(lease: Lease, keys: List<Key>): Long =
        repository.createLeaseWithKeys(lease, keys)

    override suspend fun closeLease(leaseId: Long, endEpochDay: Long) {
        repository.closeLease(leaseId, endEpochDay)
    }
}
