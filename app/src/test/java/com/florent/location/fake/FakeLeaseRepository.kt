package com.florent.location.fake

import com.florent.location.domain.model.Key
import com.florent.location.domain.model.Lease
import com.florent.location.domain.repository.LeaseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeLeaseRepository(
    existingHousingIds: Set<Long> = emptySet(),
    existingTenantIds: Set<Long> = emptySet()
) : LeaseRepository {
    private val leasesFlow = MutableStateFlow<List<Lease>>(emptyList())
    private val keysFlow = MutableStateFlow<List<Key>>(emptyList())
    private var nextLeaseId: Long = 1L
    private var nextKeyId: Long = 1L
    private val housingIds = existingHousingIds.toMutableSet()
    private val tenantIds = existingTenantIds.toMutableSet()

    override suspend fun createLeaseWithKeys(lease: Lease, keys: List<Key>): Long {
        val activeLease = leasesFlow.value.firstOrNull {
            it.housingId == lease.housingId && it.endDateEpochDay == null
        }
        require(activeLease == null) { "Un bail actif existe déjà pour ce logement." }

        val id = if (lease.id == 0L) nextLeaseId++ else lease.id
        val newLease = lease.copy(id = id)
        leasesFlow.value = leasesFlow.value + newLease

        val newKeys = keys.map { key ->
            val keyId = if (key.id == 0L) nextKeyId++ else key.id
            key.copy(id = keyId, leaseId = id)
        }
        keysFlow.value = keysFlow.value + newKeys

        return id
    }

    override fun observeActiveLeaseForHousing(housingId: Long): Flow<Lease?> =
        leasesFlow.map { leases ->
            leases.firstOrNull { it.housingId == housingId && it.endDateEpochDay == null }
        }

    override fun observeLease(leaseId: Long): Flow<Lease?> =
        leasesFlow.map { leases ->
            leases.firstOrNull { it.id == leaseId }
        }

    override fun observeKeysForLease(leaseId: Long): Flow<List<Key>> =
        keysFlow.map { keys ->
            keys.filter { it.leaseId == leaseId }
        }

    override suspend fun housingExists(housingId: Long): Boolean =
        housingId in housingIds

    override suspend fun tenantExists(tenantId: Long): Boolean =
        tenantId in tenantIds

    override suspend fun closeLease(leaseId: Long, endEpochDay: Long) {
        val leases = leasesFlow.value.toMutableList()
        val index = leases.indexOfFirst { it.id == leaseId && it.endDateEpochDay == null }
        require(index != -1) { "Bail introuvable ou déjà clôturé." }
        leases[index] = leases[index].copy(endDateEpochDay = endEpochDay)
        leasesFlow.value = leases
    }

    fun seedHousing(id: Long) {
        housingIds.add(id)
    }

    fun seedTenant(id: Long) {
        tenantIds.add(id)
    }
}
