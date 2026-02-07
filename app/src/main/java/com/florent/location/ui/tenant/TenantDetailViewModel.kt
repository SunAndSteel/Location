package com.florent.location.ui.tenant

import androidx.lifecycle.ViewModel
import com.florent.location.domain.usecase.lease.LeaseUseCases
import com.florent.location.domain.usecase.tenant.TenantUseCases

class TenantDetailViewModel(
    private val tenantId: Long,
    private val tenantUseCases: TenantUseCases,
    private val leaseUseCases: LeaseUseCases
) : ViewModel()
