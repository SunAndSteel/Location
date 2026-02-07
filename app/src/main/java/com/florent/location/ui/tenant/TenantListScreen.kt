@file:OptIn(ExperimentalMaterial3Api::class)

package com.florent.location.ui.tenant

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import com.florent.location.ui.components.AdaptiveContent
import com.florent.location.ui.components.ExpressiveEmptyState
import com.florent.location.ui.components.ExpressiveErrorState
import com.florent.location.ui.components.ExpressiveLoadingState
import com.florent.location.ui.components.TenantCard
import com.florent.location.ui.components.SectionHeader
import com.florent.location.ui.components.windowWidthSize
import com.florent.location.ui.components.WindowWidthSize
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.ListAlt

@Composable
fun TenantListScreen(
    viewModel: TenantListViewModel = koinViewModel(),
    onTenantClick: (Long) -> Unit,
    onAddTenant: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    TenantListContent(
        state = state,
        onEvent = viewModel::onEvent,
        onTenantClick = onTenantClick,
        onAddTenant = onAddTenant,
        modifier = modifier
    )
}

@Composable
private fun TenantListContent(
    state: TenantListUiState,
    onEvent: (TenantListUiEvent) -> Unit,
    onTenantClick: (Long) -> Unit,
    onAddTenant: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(text = "Locataires") }) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTenant,
                modifier = Modifier.focusable()
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Ajouter un locataire")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        AdaptiveContent(innerPadding = innerPadding, contentMaxWidth = 1080.dp) {
            BoxWithConstraints {
                val sizeClass = windowWidthSize(maxWidth)
                if (sizeClass == WindowWidthSize.Expanded) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Column(modifier = Modifier.weight(0.58f)) {
                            OutlinedTextField(
                                value = state.searchQuery,
                                onValueChange = { onEvent(TenantListUiEvent.SearchQueryChanged(it)) },
                                label = { Text(text = "Rechercher") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            TenantListBody(
                                state = state,
                                onAddTenant = onAddTenant,
                                onTenantClick = onTenantClick,
                                onEvent = onEvent
                            )
                        }
                        TenantContextPanel(
                            total = state.tenants.size,
                            selectedName = state.tenants.firstOrNull {
                                it.id == state.selectedTenantId
                            }?.let { "${it.firstName} ${it.lastName}" },
                            modifier = Modifier.weight(0.42f)
                        )
                    }
                } else {
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = { onEvent(TenantListUiEvent.SearchQueryChanged(it)) },
                        label = { Text(text = "Rechercher") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    TenantListBody(
                        state = state,
                        onAddTenant = onAddTenant,
                        onTenantClick = onTenantClick,
                        onEvent = onEvent
                    )
                }
            }
        }
    }
}

@Composable
private fun TenantListBody(
    state: TenantListUiState,
    onAddTenant: () -> Unit,
    onTenantClick: (Long) -> Unit,
    onEvent: (TenantListUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        state.isLoading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                ExpressiveLoadingState(
                    title = "Chargement des locataires",
                    message = "Nous préparons vos contacts."
                )
            }
        }

        state.errorMessage != null -> {
            Column(
                modifier = modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ExpressiveErrorState(
                    title = "Impossible de charger les locataires",
                    message = state.errorMessage,
                    icon = Icons.Outlined.ErrorOutline
                )
            }
        }

        state.isEmpty -> {
            Column(
                modifier = modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ExpressiveEmptyState(
                    title = "Aucun locataire enregistré",
                    message = "Ajoutez votre premier locataire pour démarrer un bail.",
                    icon = Icons.Outlined.Group,
                    actionLabel = "Ajouter un locataire",
                    onAction = onAddTenant
                )
            }
        }

        else -> {
            LazyColumn(
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(state.tenants, key = { it.id }) { tenant ->
                    val openTenant = {
                        onEvent(TenantListUiEvent.TenantClicked(tenant.id))
                        onTenantClick(tenant.id)
                    }
                    TenantCard(
                        tenant = tenant,
                        onOpen = openTenant,
                        onDelete = {
                            onEvent(TenantListUiEvent.DeleteTenantClicked(tenant.id))
                        },
                        isSelected = tenant.id == state.selectedTenantId,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun TenantContextPanel(
    total: Int,
    selectedName: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionHeader(
            title = "Aperçu",
            supportingText = "Suivi rapide de vos contacts."
        )
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.ListAlt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Locataires au total : $total",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Text(
                    text = selectedName?.let { "Sélectionné : $it" } ?: "Aucun locataire sélectionné.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
