package com.florent.location.domain.usecase.indexation

import com.florent.location.domain.model.UpcomingIndexation
import com.florent.location.domain.repository.LeaseRepository
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface IndexationUseCases {
    fun observeUpcomingIndexations(todayEpochDay: Long): Flow<List<UpcomingIndexation>>
}

class IndexationUseCasesImpl(
    private val leaseRepository: LeaseRepository
) : IndexationUseCases {

    override fun observeUpcomingIndexations(todayEpochDay: Long): Flow<List<UpcomingIndexation>> {
        val today = LocalDate.ofEpochDay(todayEpochDay)
        return leaseRepository.observeActiveLeases().map { leases ->
            leases.map { lease ->
                val anniversary = lease.indexAnniversaryEpochDay ?: lease.startDateEpochDay
                val nextDate = nextIndexationDate(anniversary, today)
                UpcomingIndexation(
                    leaseId = lease.id,
                    housingId = lease.housingId,
                    tenantId = lease.tenantId,
                    nextIndexationEpochDay = nextDate.toEpochDay(),
                    daysUntil = ChronoUnit.DAYS.between(today, nextDate).toInt()
                )
            }.sortedBy { it.daysUntil }
        }
    }

    private fun nextIndexationDate(anniversaryEpochDay: Long, today: LocalDate): LocalDate {
        val anniversaryDate = LocalDate.ofEpochDay(anniversaryEpochDay)
        if (!anniversaryDate.isBefore(today)) {
            return anniversaryDate
        }
        val yearsBetween = ChronoUnit.YEARS.between(anniversaryDate, today)
        var candidate = anniversaryDate.plusYears(yearsBetween)
        if (candidate.isBefore(today)) {
            candidate = candidate.plusYears(1)
        }
        return candidate
    }
}
