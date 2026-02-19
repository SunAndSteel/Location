package com.florent.location.fake

import com.florent.location.domain.model.IndexationEvent
import com.florent.location.domain.model.IndexationPolicy
import com.florent.location.domain.model.IndexationSimulation
import com.florent.location.domain.model.Lease
import com.florent.location.domain.usecase.lease.LeaseCreateRequest
import com.florent.location.domain.usecase.lease.LeaseUseCases
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeLeaseUseCases : LeaseUseCases {
    override suspend fun createLease(request: LeaseCreateRequest): Long = 0L

    override fun observeLease(leaseId: Long): Flow<Lease?> = flowOf(null)

    override fun observeLeases(): Flow<List<Lease>> = flowOf(emptyList())

    override fun observeActiveLeaseForHousing(housingId: Long): Flow<Lease?> = flowOf(null)

    override fun observeActiveLeaseForTenant(tenantId: Long): Flow<Lease?> = flowOf(null)

    override fun observeIndexationEvents(leaseId: Long): Flow<List<IndexationEvent>> = flowOf(emptyList())

    override fun buildIndexationPolicy(lease: Lease, todayEpochDay: Long): IndexationPolicy =
        IndexationPolicy(
            anniversaryEpochDay = lease.indexAnniversaryEpochDay ?: lease.startDateEpochDay,
            nextIndexationEpochDay = todayEpochDay
        )

    override suspend fun simulateIndexation(
        leaseId: Long,
        indexPercent: Double,
        effectiveEpochDay: Long
    ): IndexationSimulation =
        IndexationSimulation(
            leaseId = leaseId,
            baseRentCents = 0L,
            indexPercent = indexPercent,
            newRentCents = 0L,
            effectiveEpochDay = effectiveEpochDay
        )

    override suspend fun applyIndexation(
        leaseId: Long,
        indexPercent: Double,
        effectiveEpochDay: Long
    ): IndexationEvent =
        IndexationEvent(
            leaseId = leaseId,
            appliedEpochDay = effectiveEpochDay,
            baseRentCents = 0L,
            indexPercent = indexPercent,
            newRentCents = 0L
        )

    override suspend fun closeLease(leaseId: Long, endEpochDay: Long) = Unit
}
