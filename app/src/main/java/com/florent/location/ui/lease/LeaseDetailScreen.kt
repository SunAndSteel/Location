package com.florent.location.ui.lease

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.florent.location.ui.lease.components.LeaseDetailContent

@Composable
fun LeaseDetailScreen(
    viewModel: LeaseDetailViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    LeaseDetailContent(
        state = state,
        onEvent = viewModel::onEvent,
        modifier = modifier
    )
}
