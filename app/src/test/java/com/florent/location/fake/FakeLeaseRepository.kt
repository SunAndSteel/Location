package com.florent.location.fake

import com.florent.location.domain.model.Key
import com.florent.location.domain.model.Lease
import com.florent.location.domain.repository.LeaseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeLeaseRepository(
    leases: List<Lease>,
    keys: List<Key>
) : LeaseRepository {
    private val leaseFlow = MutableStateFlow(leases.associateBy { it.id })
    private val keysFlow = MutableStateFlow(keys.groupBy { it.leaseId })
    private var nextKeyId: Long = (keys.maxOfOrNull { it.id } ?: 0L) + 1L

    override suspend fun createLeaseWithKeys(lease: Lease, keys: List<Key>): Long {
        val leaseId = lease.id.takeIf { it != 0L } ?: (leaseFlow.value.keys.maxOrNull() ?: 0L) + 1L
        val newLease = lease.copy(id = leaseId)
        leaseFlow.value = leaseFlow.value + (leaseId to newLease)
        if (keys.isNotEmpty()) {
            val updatedKeys = keys.map { key ->
                val id = nextKeyId++
                key.copy(id = id, leaseId = leaseId)
            }
            keysFlow.value = keysFlow.value + (leaseId to updatedKeys)
        }
        return leaseId
    }

    override fun observeActiveLeaseForHousing(housingId: Long): Flow<Lease?> {
        return leaseFlow.map { leases ->
            leases.values.firstOrNull { it.housingId == housingId && it.endDateEpochDay == null }
        }
    }

    override fun observeLease(leaseId: Long): Flow<Lease?> {
        return leaseFlow.map { it[leaseId] }
    }

    override fun observeKeysForLease(leaseId: Long): Flow<List<Key>> {
        return keysFlow.map { it[leaseId].orEmpty() }
    }

    override suspend fun insertKey(key: Key): Long {
        val lease = leaseFlow.value[key.leaseId]
            ?: throw IllegalArgumentException("Bail introuvable.")
        val id = nextKeyId++
        val newKey = key.copy(id = id, leaseId = lease.id)
        val updated = keysFlow.value[key.leaseId].orEmpty() + newKey
        keysFlow.value = keysFlow.value + (key.leaseId to updated)
        return id
    }

    override suspend fun deleteKeyById(id: Long) {
        val current = keysFlow.value
        val entry = current.entries.firstOrNull { (_, keys) -> keys.any { it.id == id } }
            ?: throw IllegalArgumentException("Clé introuvable.")
        val leaseId = entry.key
        val updatedKeys = entry.value.filterNot { it.id == id }
        keysFlow.value = current + (leaseId to updatedKeys)
    }

    override suspend fun housingExists(housingId: Long): Boolean = true

    override suspend fun tenantExists(tenantId: Long): Boolean = true

    override suspend fun closeLease(leaseId: Long, endEpochDay: Long) {
        val lease = leaseFlow.value[leaseId]
            ?: throw IllegalArgumentException("Bail introuvable.")
        leaseFlow.value = leaseFlow.value + (leaseId to lease.copy(endDateEpochDay = endEpochDay))
    }

    companion object {
        const val ACTIVE_LEASE_ID = 1L
        const val ACTIVE_HOUSING_ID = 10L
        const val ACTIVE_TENANT_ID = 20L
        const val START_EPOCH_DAY = 19500L
        const val CLOSE_EPOCH_DAY = 19600L
        const val KEY_ID_1 = 101L
        const val KEY_ID_2 = 102L

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
                mailboxLabel = "B",
                meterGas = "G1",
                meterElectricity = "E1",
                meterWater = "W1",
                indexAnniversaryEpochDay = START_EPOCH_DAY
            )
            val keys = listOf(
                Key(
                    id = KEY_ID_1,
                    leaseId = ACTIVE_LEASE_ID,
                    type = "Clé",
                    deviceLabel = "Entrée",
                    handedOverEpochDay = START_EPOCH_DAY
                ),
                Key(
                    id = KEY_ID_2,
                    leaseId = ACTIVE_LEASE_ID,
                    type = "Badge",
                    deviceLabel = "Garage",
                    handedOverEpochDay = START_EPOCH_DAY
                )
            )
            return FakeLeaseRepository(listOf(lease), keys)
        }
    }
}
