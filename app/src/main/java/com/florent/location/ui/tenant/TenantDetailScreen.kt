package com.florent.location.ui.tenant

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.florent.location.ui.tenant.components.TenantDetailContent

@Composable
fun TenantDetailScreen(
    viewModel: TenantDetailViewModel,
    onEdit: () -> Unit,
    onCreateLease: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    TenantDetailContent(
        state = state,
        onEvent = viewModel::onEvent,
        onEdit = onEdit,
        onCreateLease = onCreateLease,
        modifier = modifier
    )
}
