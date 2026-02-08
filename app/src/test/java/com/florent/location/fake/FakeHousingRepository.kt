package com.florent.location.fake

import com.florent.location.domain.model.Housing
import com.florent.location.domain.model.Key
import com.florent.location.domain.repository.HousingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeHousingRepository(
    initialHousings: List<Housing> = emptyList(),
    initialKeys: List<Key> = emptyList()
) : HousingRepository {
    private val housingsFlow = MutableStateFlow(initialHousings)
    private var nextId: Long =
        (initialHousings.maxOfOrNull { it.id } ?: 0L) + 1L
    private val activeLeaseHousingIds = mutableSetOf<Long>()
    private val keysFlow = MutableStateFlow(initialKeys.groupBy { it.housingId })
    private var nextKeyId: Long = (initialKeys.maxOfOrNull { it.id } ?: 0L) + 1L

    override fun observeHousings(): Flow<List<Housing>> = housingsFlow

    override fun observeHousing(id: Long): Flow<Housing?> =
        housingsFlow.map { housings -> housings.firstOrNull { it.id == id } }

    override suspend fun insert(housing: Housing): Long {
        val id = if (housing.id == 0L) nextId++ else housing.id
        val newHousing = housing.copy(id = id)
        housingsFlow.value = housingsFlow.value + newHousing
        return id
    }

    override suspend fun update(housing: Housing) {
        val housings = housingsFlow.value.toMutableList()
        val index = housings.indexOfFirst { it.id == housing.id }
        require(index != -1) { "Logement introuvable." }
        housings[index] = housing
        housingsFlow.value = housings
    }

    override suspend fun deleteById(id: Long) {
        housingsFlow.value = housingsFlow.value.filterNot { it.id == id }
    }

    override fun observeKeysForHousing(housingId: Long): Flow<List<Key>> =
        keysFlow.map { keys -> keys[housingId].orEmpty() }

    override suspend fun insertKey(key: Key): Long {
        val housing = housingsFlow.value.firstOrNull { it.id == key.housingId }
            ?: throw IllegalArgumentException("Logement introuvable.")
        val id = nextKeyId++
        val newKey = key.copy(id = id, housingId = housing.id)
        val updated = keysFlow.value[housing.id].orEmpty() + newKey
        keysFlow.value = keysFlow.value + (housing.id to updated)
        return id
    }

    override suspend fun deleteKeyById(id: Long) {
        val current = keysFlow.value
        val entry = current.entries.firstOrNull { (_, keys) -> keys.any { it.id == id } }
            ?: throw IllegalArgumentException("Clé introuvable.")
        val housingId = entry.key
        val updatedKeys = entry.value.filterNot { it.id == id }
        keysFlow.value = current + (housingId to updatedKeys)
    }

    override suspend fun hasActiveLease(housingId: Long): Boolean =
        housingId in activeLeaseHousingIds

    fun setHousings(housings: List<Housing>) {
        housingsFlow.value = housings
    }

    fun setActiveLease(housingId: Long, hasActiveLease: Boolean) {
        if (hasActiveLease) {
            activeLeaseHousingIds.add(housingId)
        } else {
            activeLeaseHousingIds.remove(housingId)
        }
    }
}
