package com.florent.location.fake

import com.florent.location.domain.model.Lease
import com.florent.location.domain.usecase.lease.LeaseCreateRequest
import com.florent.location.domain.usecase.lease.LeaseUseCases
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeLeaseUseCases : LeaseUseCases {
    override suspend fun createLease(request: LeaseCreateRequest): Long = 0L

    override fun observeLease(leaseId: Long): Flow<Lease?> = flowOf(null)

    override suspend fun closeLease(leaseId: Long, endEpochDay: Long) = Unit
}
