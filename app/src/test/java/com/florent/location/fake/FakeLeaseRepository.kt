package com.florent.location.fake

import com.florent.location.domain.model.Lease
import com.florent.location.domain.model.IndexationEvent
import com.florent.location.domain.repository.LeaseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeLeaseRepository(
    leases: List<Lease> = emptyList(),
    private val existingHousingIds: Set<Long> = emptySet(),
    private val existingTenantIds: Set<Long> = emptySet()
) : LeaseRepository {
    private val leaseFlow = MutableStateFlow(leases.associateBy { it.id })
    private val indexationEventsFlow = MutableStateFlow(emptyMap<Long, List<IndexationEvent>>())

    override suspend fun createLease(lease: Lease): Long {
        val leaseId = lease.id.takeIf { it != 0L } ?: (leaseFlow.value.keys.maxOrNull() ?: 0L) + 1L
        val newLease = lease.copy(id = leaseId)
        val existing = leaseFlow.value.values.firstOrNull {
            it.housingId == newLease.housingId && it.endDateEpochDay == null
        }
        require(existing == null) { "Un bail actif existe déjà pour ce logement." }
        leaseFlow.value = leaseFlow.value + (leaseId to newLease)
        return leaseId
    }

    override fun observeActiveLeaseForHousing(housingId: Long): Flow<Lease?> {
        return leaseFlow.map { leases ->
            leases.values.firstOrNull { it.housingId == housingId && it.endDateEpochDay == null }
        }
    }

    override fun observeActiveLeaseForTenant(tenantId: Long): Flow<Lease?> {
        return leaseFlow.map { leases ->
            leases.values.firstOrNull { it.tenantId == tenantId && it.endDateEpochDay == null }
        }
    }

    override fun observeActiveLeases(): Flow<List<Lease>> {
        return leaseFlow.map { leases ->
            leases.values.filter { it.endDateEpochDay == null }
        }
    }

    override fun observeLease(leaseId: Long): Flow<Lease?> {
        return leaseFlow.map { it[leaseId] }
    }

    override suspend fun getLease(leaseId: Long): Lease? = leaseFlow.value[leaseId]

    override suspend fun housingExists(housingId: Long): Boolean =
        existingHousingIds.isEmpty() || existingHousingIds.contains(housingId)

    override suspend fun tenantExists(tenantId: Long): Boolean =
        existingTenantIds.isEmpty() || existingTenantIds.contains(tenantId)

    override suspend fun closeLease(leaseId: Long, endEpochDay: Long) {
        val lease = leaseFlow.value[leaseId]
            ?: throw IllegalArgumentException("Bail introuvable.")
        leaseFlow.value = leaseFlow.value + (leaseId to lease.copy(endDateEpochDay = endEpochDay))
    }

    override fun observeIndexationEvents(leaseId: Long): Flow<List<IndexationEvent>> {
        return indexationEventsFlow.map { events -> events[leaseId].orEmpty() }
    }

    override suspend fun applyIndexation(event: IndexationEvent) {
        val current = indexationEventsFlow.value[event.leaseId].orEmpty()
        indexationEventsFlow.value = indexationEventsFlow.value + (event.leaseId to (current + event))
    }

    companion object {
        const val ACTIVE_LEASE_ID = 1L
        const val ACTIVE_HOUSING_ID = 10L
        const val ACTIVE_TENANT_ID = 20L
        const val START_EPOCH_DAY = 19500L
        const val CLOSE_EPOCH_DAY = 19600L
        fun seeded(): FakeLeaseRepository {
            val lease = Lease(
                id = ACTIVE_LEASE_ID,
                housingId = ACTIVE_HOUSING_ID,
                tenantId = ACTIVE_TENANT_ID,
                startDateEpochDay = START_EPOCH_DAY,
                endDateEpochDay = null,
                rentCents = 100000,
                chargesCents = 20000,
                depositCents = 50000,
                rentDueDayOfMonth = 5,
                indexAnniversaryEpochDay = START_EPOCH_DAY,
                rentOverridden = false,
                chargesOverridden = false,
                depositOverridden = false,
                housingRentCentsSnapshot = 100000,
                housingChargesCentsSnapshot = 20000,
                housingDepositCentsSnapshot = 50000
            )
            return FakeLeaseRepository(listOf(lease))
        }
    }
}
