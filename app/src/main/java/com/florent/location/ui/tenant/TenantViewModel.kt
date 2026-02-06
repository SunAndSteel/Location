package com.florent.location.ui.tenant


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.florent.location.data.db.TenantDao
import kotlinx.coroutines.launch

class TenantViewModel(
    private val tenantDao: TenantDao
) : ViewModel() {

    init {
        // Juste pour vérifier: la DB est créée + DAO injecté + une requête passe
        viewModelScope.launch {
            tenantDao.observeAll().collect { list ->
                // No-op, le but est juste de ne pas crasher
                // (Tu peux logger list.size si tu veux)
            }
        }
    }
}
