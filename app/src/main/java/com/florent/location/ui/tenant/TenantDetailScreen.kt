package com.florent.location.ui.tenant

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.florent.location.ui.tenant.components.TenantActionsBottomSheet
import com.florent.location.ui.tenant.components.TenantDetailContent

@Composable
fun TenantDetailScreen(
    viewModel: TenantDetailViewModel,
    onEdit: () -> Unit,
    onCreateLease: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    var showActionsSheet by remember { mutableStateOf(false) }

    TenantDetailContent(
        state = state,
        onEvent = viewModel::onEvent,
        onEdit = onEdit,
        onCreateLease = onCreateLease,
        onShowActions = { showActionsSheet = true },
        modifier = modifier
    )

    if (showActionsSheet) {
        TenantActionsBottomSheet(
            onEdit = {
                showActionsSheet = false
                viewModel.onEvent(TenantDetailUiEvent.Edit)
                onEdit()
            },
            onCreateLease = {
                showActionsSheet = false
                onCreateLease()
            },
            onDelete = {
                showActionsSheet = false
                viewModel.onEvent(TenantDetailUiEvent.Delete)
            },
            onDismiss = { showActionsSheet = false }
        )
    }
}
