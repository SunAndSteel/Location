package com.florent.location.domain.usecase.indexation

import com.florent.location.domain.repository.LeaseRepository

interface IndexationUseCases

class IndexationUseCasesImpl(
    private val leaseRepository: LeaseRepository
) : IndexationUseCases
