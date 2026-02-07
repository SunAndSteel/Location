package com.florent.location.ui.tenant

import androidx.lifecycle.ViewModel
import com.florent.location.domain.usecase.tenant.TenantUseCases

class TenantEditViewModel(
    private val tenantId: Long?,
    private val useCases: TenantUseCases
) : ViewModel()
