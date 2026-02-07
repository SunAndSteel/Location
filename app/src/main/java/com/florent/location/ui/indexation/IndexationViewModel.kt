package com.florent.location.ui.indexation

import androidx.lifecycle.ViewModel
import com.florent.location.domain.usecase.indexation.IndexationUseCases

class IndexationViewModel(
    private val useCases: IndexationUseCases
) : ViewModel()
