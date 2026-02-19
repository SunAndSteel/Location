package com.florent.location.ui.housing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.florent.location.ui.components.LabeledValueRow
import com.florent.location.ui.components.windowWidthSize
import com.florent.location.ui.housing.components.HousingActionsBottomSheet
import com.florent.location.ui.housing.components.HousingDeleteConfirmationDialog
import com.florent.location.ui.housing.components.HousingDetailSidePanel
import com.florent.location.ui.housing.components.HousingHeroSection
import com.florent.location.ui.housing.components.HousingTopBar
import com.florent.location.ui.housing.components.HousingCharacteristicsContent
import com.florent.location.ui.components.formatCurrency
import com.florent.location.ui.components.formatEpochDay

@Composable
fun HousingDetailScreen(
    viewModel: HousingDetailViewModel,
    initialTab: HousingDetailTab = HousingDetailTab.HOUSING,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onCreateLease: () -> Unit,
    onDeleted: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    var selectedTab by remember(initialTab) { mutableStateOf(initialTab) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showActionsSheet by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier = modifier) {
        val isExpanded = windowWidthSize(maxWidth) == com.florent.location.ui.components.WindowWidthSize.Expanded

        Scaffold(
            topBar = {
                HousingTopBar(
                    housingAddress = state.housing?.address?.fullString() ?: "",
                    onBack = onBack,
                    onEdit = onEdit,
                    onCreateLease = onCreateLease,
                    onDelete = { showDeleteDialog = true },
                    isCompact = !isExpanded,
                    onShowActions = { showActionsSheet = true }
                )
            }
        ) { innerPadding ->
            if (isExpanded) {
                Row(
                    Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        state.housing?.let { HousingHeroSection(it, state.situation) }
                        HousingDetailTabRow(selectedTab, state, onTabSelected = { selectedTab = it })
                        HousingDetailTabContent(selectedTab, state, viewModel::onEvent)
                    }
                    HousingDetailSidePanel(
                        state = state,
                        onCreateLease = onCreateLease,
                        modifier = Modifier.width(340.dp).fillMaxHeight()
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item { state.housing?.let { HousingHeroSection(it, state.situation) } }
                    item { HousingDetailTabRow(selectedTab, state, onTabSelected = { selectedTab = it }) }
                    item { HousingDetailTabContent(selectedTab, state, viewModel::onEvent) }
                    item { HousingDetailSidePanel(state, onCreateLease) }
                }
            }
        }
    }

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

@Composable
fun HousingDetailTabRow(
    selectedTab: HousingDetailTab,
    state: HousingDetailUiState,
    onTabSelected: (HousingDetailTab) -> Unit
) {
    TabRow(selectedTabIndex = selectedTab.ordinal) {
        Tab(
            selected = selectedTab == HousingDetailTab.HOUSING,
            onClick = { onTabSelected(HousingDetailTab.HOUSING) },
            text = { Text("Logement") }
        )
        Tab(
            selected = selectedTab == HousingDetailTab.BAIL,
            enabled = state.lease != null,
            onClick = { onTabSelected(HousingDetailTab.BAIL) },
            text = { Text("Bail") }
        )
        Tab(
            selected = selectedTab == HousingDetailTab.TENANT,
            enabled = state.tenant != null,
            onClick = { onTabSelected(HousingDetailTab.TENANT) },
            text = { Text("Locataire") }
        )
    }
}

@Composable
fun HousingDetailTabContent(
    tab: HousingDetailTab,
    state: HousingDetailUiState,
    onEvent: (HousingDetailUiEvent) -> Unit
) {
    when (tab) {
        HousingDetailTab.HOUSING -> HousingCharacteristicsContent(state.housing)
        HousingDetailTab.BAIL -> LeaseTabContent(state, onEvent)
        HousingDetailTab.TENANT -> TenantTabContent(state)
    }
}

@Composable
private fun LeaseTabContent(state: HousingDetailUiState, onEvent: (HousingDetailUiEvent) -> Unit) {
    val lease = state.lease ?: run {
        Text("Aucun bail actif.", modifier = Modifier.padding(12.dp))
        return
    }
    Card {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            LabeledValueRow("Début", formatEpochDay(lease.startDateEpochDay))
            LabeledValueRow("Fin", lease.endDateEpochDay?.let { formatEpochDay(it) } ?: "Sans terme fixe")
            LabeledValueRow("Loyer CC", formatCurrency(lease.rentCents + lease.chargesCents))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onEvent(HousingDetailUiEvent.AddKeyClicked) }) { Text("Ajouter une clé") }
                Button(onClick = { onEvent(HousingDetailUiEvent.CloseLeaseClicked) }) { Text("Clôturer") }
            }
        }
    }
}

@Composable
private fun TenantTabContent(state: HousingDetailUiState) {
    val tenant = state.tenant ?: run {
        Text("Aucun locataire.", modifier = Modifier.padding(12.dp))
        return
    }
    Card {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("${tenant.firstName} ${tenant.lastName}", style = MaterialTheme.typography.titleMedium)
            LabeledValueRow("Email", tenant.email ?: "-")
            LabeledValueRow("Téléphone", tenant.phone ?: "-")
        }
    }
}
