package com.florent.location.ui.lease

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.florent.location.ui.lease.components.LeaseActionsBottomSheet
import com.florent.location.ui.lease.components.LeaseDetailContent

@Composable
fun LeaseDetailScreen(
    viewModel: LeaseDetailViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    var showActionsSheet by remember { mutableStateOf(false) }

    LeaseDetailContent(
        state = state,
        onEvent = viewModel::onEvent,
        onShowActions = { showActionsSheet = true },
        onNavigateBack = onNavigateBack,
        modifier = modifier
    )

    if (showActionsSheet) {
        LeaseActionsBottomSheet(
            isActive = state.isActive,
            onAddKey = {
                showActionsSheet = false
                viewModel.onEvent(LeaseDetailUiEvent.AddKeyClicked)
            },
            onCloseLease = {
                showActionsSheet = false
                viewModel.onEvent(LeaseDetailUiEvent.CloseLeaseClicked)
            },
            onDismiss = { showActionsSheet = false }
        )
    }
}
