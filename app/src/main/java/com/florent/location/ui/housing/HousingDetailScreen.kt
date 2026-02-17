package com.florent.location.ui.housing

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.florent.location.ui.housing.components.HousingActionsBottomSheet
import com.florent.location.ui.housing.components.HousingDeleteConfirmationDialog
import com.florent.location.ui.housing.components.HousingDetailContent

@Composable
fun HousingDetailScreen(
    viewModel: HousingDetailViewModel,
    onEdit: () -> Unit,
    onCreateLease: () -> Unit,
    onNavigateBack: () -> Unit,
    onDeleted: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showActionsSheet by remember { mutableStateOf(false) }

    HousingDetailContent(
        state = state,
        onEdit = onEdit,
        onCreateLease = onCreateLease,
        onDeleteClick = { showDeleteDialog = true },
        onShowActions = { showActionsSheet = true },
        onNavigateBack = onNavigateBack,
        modifier = modifier
    )

    if (showDeleteDialog) {
        HousingDeleteConfirmationDialog(
            housingAddress = state.housing?.address?.fullString() ?: "",
            onConfirm = {
                state.housing?.let { housing ->
                    viewModel.onEvent(HousingDetailUiEvent.DeleteHousing(housing.id))
                }
                showDeleteDialog = false
                onDeleted()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    if (showActionsSheet) {
        HousingActionsBottomSheet(
            onEdit = {
                showActionsSheet = false
                onEdit()
            },
            onCreateLease = {
                showActionsSheet = false
                onCreateLease()
            },
            onDelete = {
                showActionsSheet = false
                showDeleteDialog = true
            },
            onDismiss = { showActionsSheet = false }
        )
    }
}
