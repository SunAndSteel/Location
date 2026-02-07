package com.florent.location.ui.lease

import androidx.lifecycle.ViewModel
import com.florent.location.domain.usecase.lease.LeaseUseCases

class LeaseDetailViewModel(
    private val leaseId: Long,
    private val leaseUseCases: LeaseUseCases
) : ViewModel()
