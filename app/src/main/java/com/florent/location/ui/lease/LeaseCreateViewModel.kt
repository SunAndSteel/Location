package com.florent.location.ui.lease

import androidx.lifecycle.ViewModel
import com.florent.location.domain.usecase.housing.HousingUseCases
import com.florent.location.domain.usecase.lease.LeaseUseCases
import com.florent.location.domain.usecase.tenant.TenantUseCases

class LeaseCreateViewModel(
    private val housingUseCases: HousingUseCases,
    private val tenantUseCases: TenantUseCases,
    private val leaseUseCases: LeaseUseCases
) : ViewModel()
