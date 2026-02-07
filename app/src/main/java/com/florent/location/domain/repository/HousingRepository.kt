package com.florent.location.domain.repository

import com.florent.location.domain.model.Housing
import kotlinx.coroutines.flow.Flow

/**
 * Contrat de persistence pour les logements.
 */
interface HousingRepository {
    /**
     * Observe la liste complète des logements.
     */
    fun observeHousings(): Flow<List<Housing>>

    /**
     * Observe un logement par identifiant.
     */
    fun observeHousing(id: Long): Flow<Housing?>

    /**
     * Insère un logement et renvoie son identifiant.
     */
    suspend fun insert(housing: Housing): Long

    /**
     * Met à jour un logement.
     */
    suspend fun update(housing: Housing)

    /**
     * Supprime un logement par identifiant.
     */
    suspend fun deleteById(id: Long)

    /**
     * Indique si un bail actif existe pour ce logement.
     */
    suspend fun hasActiveLease(housingId: Long): Boolean
}
