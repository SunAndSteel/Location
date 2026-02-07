package com.florent.location.domain.repository

import com.florent.location.domain.model.IndexationEvent
import com.florent.location.domain.model.Key
import com.florent.location.domain.model.Lease
import kotlinx.coroutines.flow.Flow

interface LeaseRepository {
    suspend fun createLeaseWithKeys(lease: Lease, keys: List<Key>): Long
    fun observeActiveLeases(): Flow<List<Lease>>
    fun observeActiveLeaseForHousing(housingId: Long): Flow<Lease?>
    fun observeLease(leaseId: Long): Flow<Lease?>
    suspend fun getLease(leaseId: Long): Lease?
    fun observeKeysForLease(leaseId: Long): Flow<List<Key>>
    suspend fun insertKey(key: Key): Long
    suspend fun deleteKeyById(id: Long)
    suspend fun housingExists(housingId: Long): Boolean
    suspend fun tenantExists(tenantId: Long): Boolean
    suspend fun closeLease(leaseId: Long, endEpochDay: Long)
    fun observeIndexationEvents(leaseId: Long): Flow<List<IndexationEvent>>
    suspend fun applyIndexation(event: IndexationEvent)
}
