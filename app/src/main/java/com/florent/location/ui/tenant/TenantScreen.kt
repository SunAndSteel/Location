package com.florent.location.ui.tenant

import androidx.compose.runtime.Composable
import org.koin.androidx.compose.koinViewModel

@Composable
fun TenantScreen(
    viewModel: TenantListViewModel = koinViewModel()
) {
    TenantListScreen(
        viewModel = viewModel,
        onTenantClick = {},
        onAddTenant = {}
    )
}
