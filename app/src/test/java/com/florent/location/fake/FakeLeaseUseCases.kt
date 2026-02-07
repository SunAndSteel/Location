package com.florent.location.fake

import com.florent.location.domain.model.Key
import com.florent.location.domain.model.Lease
import com.florent.location.domain.usecase.lease.LeaseUseCases

class FakeLeaseUseCases : LeaseUseCases {
    override suspend fun createLeaseWithKeys(lease: Lease, keys: List<Key>): Long = 0L

    override suspend fun closeLease(leaseId: Long, endEpochDay: Long) = Unit
}
