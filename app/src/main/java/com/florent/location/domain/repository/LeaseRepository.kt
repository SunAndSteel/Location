package com.florent.location.domain.repository

import com.florent.location.domain.model.Key
import com.florent.location.domain.model.Lease

interface LeaseRepository {
    suspend fun createLeaseWithKeys(lease: Lease, keys: List<Key>): Long
    suspend fun closeLease(leaseId: Long, endEpochDay: Long)
}