package com.florent.location.ui.tenant

import androidx.lifecycle.ViewModel
import com.florent.location.domain.usecase.tenant.TenantUseCases

class TenantListViewModel(
    private val useCases: TenantUseCases
) : ViewModel()
