package com.florent.location.domain.usecase.housing

import com.florent.location.domain.model.Housing
import com.florent.location.domain.model.HousingSituation
import com.florent.location.domain.repository.LeaseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Observe la situation d'occupation d'un logement.
 */
class ObserveHousingSituation(
    private val leaseRepository: LeaseRepository
) {
    operator fun invoke(housing: Housing): Flow<HousingSituation> =
        leaseRepository.observeActiveLeaseForHousing(housing.id).map { lease ->
            when {
                lease != null -> HousingSituation.OCCUPE
                housing.defaultRentCents == 0L &&
                    housing.defaultChargesCents == 0L &&
                    housing.depositCents == 0L -> HousingSituation.DRAFT
                else -> HousingSituation.LIBRE
            }
        }
}
