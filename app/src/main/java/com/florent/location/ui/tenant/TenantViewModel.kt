package com.florent.location.ui.tenant


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.florent.location.data.db.dao.TenantDao
import com.florent.location.domain.usecase.TenantUseCases
import kotlinx.coroutines.launch

class TenantViewModel(
    private val useCases: TenantUseCases
) : ViewModel() {

    init {
        // Juste pour vérifier: la DB est créée + DAO injecté + une requête passe
        viewModelScope.launch {
            useCases.observeAll().collect { list ->
                // No-op, le but est juste de ne pas crasher
                // (Tu peux logger list.size si tu veux)
            }
        }
    }
}
