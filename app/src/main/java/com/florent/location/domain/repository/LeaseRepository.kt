package com.florent.location.domain.repository

import com.florent.location.domain.model.IndexationEvent
import com.florent.location.domain.model.Lease
import kotlinx.coroutines.flow.Flow

interface LeaseRepository {
    suspend fun createLease(lease: Lease): Long
    fun observeActiveLeases(): Flow<List<Lease>>
    fun observeActiveLeaseForHousing(housingId: Long): Flow<Lease?>
    fun observeActiveLeaseForTenant(tenantId: Long): Flow<Lease?>
    fun observeLease(leaseId: Long): Flow<Lease?>
    suspend fun getLease(leaseId: Long): Lease?
    suspend fun housingExists(housingId: Long): Boolean
    suspend fun tenantExists(tenantId: Long): Boolean
    suspend fun closeLease(leaseId: Long, endEpochDay: Long)
    fun observeIndexationEvents(leaseId: Long): Flow<List<IndexationEvent>>
    suspend fun applyIndexation(event: IndexationEvent)
}
