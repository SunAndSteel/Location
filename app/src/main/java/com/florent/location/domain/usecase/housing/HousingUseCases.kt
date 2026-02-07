package com.florent.location.domain.usecase.housing

import com.florent.location.domain.model.Housing
import com.florent.location.domain.repository.HousingRepository
import kotlinx.coroutines.flow.Flow

/**
 * Cas d'usage disponibles pour la gestion des logements.
 */
interface HousingUseCases {
    /**
     * Observe l'ensemble des logements.
     */
    fun observeHousings(): Flow<List<Housing>>

    /**
     * Observe un logement par identifiant.
     */
    fun observeHousing(id: Long): Flow<Housing?>

    /**
     * Crée un logement et renvoie l'identifiant.
     */
    suspend fun createHousing(housing: Housing): Long

    /**
     * Met à jour un logement.
     */
    suspend fun updateHousing(housing: Housing)

    /**
     * Supprime un logement par identifiant.
     * La suppression est interdite si un bail actif existe.
     */
    suspend fun deleteHousing(id: Long)
}

/**
 * Implémentation des cas d'usage basés sur le [HousingRepository].
 */
class HousingUseCasesImpl(
    private val repository: HousingRepository
) : HousingUseCases {

    /**
     * Observe tous les logements.
     */
    override fun observeHousings(): Flow<List<Housing>> =
        repository.observeHousings()

    /**
     * Observe un logement par identifiant.
     */
    override fun observeHousing(id: Long): Flow<Housing?> =
        repository.observeHousing(id)

    /**
     * Crée un logement et renvoie l'identifiant.
     */
    override suspend fun createHousing(housing: Housing): Long =
        repository.insert(housing)

    /**
     * Met à jour un logement.
     */
    override suspend fun updateHousing(housing: Housing) {
        repository.update(housing)
    }

    /**
     * Supprime un logement si aucun bail actif n'existe.
     */
    override suspend fun deleteHousing(id: Long) {
        require(!repository.hasActiveLease(id)) {
            "Suppression impossible: un bail actif existe pour ce logement."
        }
        repository.deleteById(id)
    }
}
