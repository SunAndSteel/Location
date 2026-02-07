package com.florent.location.ui.tenant

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.androidx.compose.koinViewModel

@Composable
fun TenantScreen(
    viewModel: TenantListViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    TenantListScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onTenantClick = {}
    )
}
