package com.florent.location.fake

import com.florent.location.domain.model.Housing
import com.florent.location.domain.repository.HousingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeHousingRepository(
    initialHousings: List<Housing> = emptyList()
) : HousingRepository {
    private val housingsFlow = MutableStateFlow(initialHousings)
    private var nextId: Long =
        (initialHousings.maxOfOrNull { it.id } ?: 0L) + 1L
    private val activeLeaseHousingIds = mutableSetOf<Long>()

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
