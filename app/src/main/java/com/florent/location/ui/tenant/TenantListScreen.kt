@file:OptIn(ExperimentalMaterial3Api::class)

package com.florent.location.ui.tenant

import android.widget.Toast
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import com.florent.location.ui.components.AppSearchBar
import com.florent.location.ui.components.ExpressiveEmptyState
import com.florent.location.ui.components.ExpressiveErrorState
import com.florent.location.ui.components.ExpressiveLoadingState
import com.florent.location.ui.components.ScreenScaffold
import com.florent.location.ui.components.TenantCard
import com.florent.location.ui.components.SectionHeader
import com.florent.location.ui.components.SectionCard
import com.florent.location.ui.components.UiTokens
import com.florent.location.ui.components.windowWidthSize
import com.florent.location.ui.components.WindowWidthSize
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.ListAlt

@Composable
fun TenantListScreen(
    viewModel: TenantListViewModel = koinViewModel(),
    onTenantClick: (housingId: Long) -> Unit,
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
    onTenantClick: (housingId: Long) -> Unit,
    onAddTenant: () -> Unit,
    modifier: Modifier = Modifier
) {
    ScreenScaffold(
        title = "Locataires",
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTenant,
                modifier = Modifier.focusable()
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Ajouter un locataire")
            }
        },
        contentMaxWidth = UiTokens.ContentMaxWidthExpanded,
        modifier = modifier
    ) {
        BoxWithConstraints {
            val sizeClass = windowWidthSize(maxWidth)
            val searchField: @Composable () -> Unit = {
                SectionCard {
                    AppSearchBar(
                        query = state.searchQuery,
                        onQueryChange = { onEvent(TenantListUiEvent.SearchQueryChanged(it)) },
                        placeholder = "Rechercher un locataire"
                    )
                }
            }
            if (sizeClass == WindowWidthSize.Expanded) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(UiTokens.SpacingXL)
                ) {
                    Column(
                        modifier = Modifier.weight(0.4f),
                        verticalArrangement = Arrangement.spacedBy(UiTokens.SpacingL)
                    ) {
                        searchField()
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
                            it.tenant.id == state.selectedTenantId
                        }?.let { "${it.tenant.firstName} ${it.tenant.lastName}" },
                        modifier = Modifier.weight(0.6f)
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(UiTokens.SpacingL)) {
                    searchField()
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
    onTenantClick: (housingId: Long) -> Unit,
    onEvent: (TenantListUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

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
                    title = "Aucun locataire",
                    message = "Ajoutez votre premier locataire pour démarrer un bail.",
                    icon = Icons.Outlined.Group,
                    actionLabel = "Ajouter un locataire",
                    onAction = onAddTenant
                )
            }
        }

        else -> {
            LazyColumn(
                contentPadding = PaddingValues(vertical = UiTokens.SpacingS),
                verticalArrangement = Arrangement.spacedBy(UiTokens.SpacingL)
            ) {
                items(state.tenants, key = { it.tenant.id }) { item ->
                    val tenant = item.tenant
                    val openTenant = {
                        onEvent(TenantListUiEvent.TenantClicked(tenant.id))
                        val housingId = item.activeHousingId
                        if (housingId != null) {
                            onTenantClick(housingId)
                        } else {
                            Toast.makeText(
                                context,
                                "Ce locataire n'a pas de logement actif.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    TenantCard(
                        tenant = tenant,
                        situation = item.situation,
                        onOpen = openTenant,
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
        verticalArrangement = Arrangement.spacedBy(UiTokens.SpacingL)
    ) {
        SectionHeader(
            title = "Conseils & raccourcis",
            supportingText = "Navigation clavier et aperçu rapide."
        )
        SectionCard(tonalColor = MaterialTheme.colorScheme.surfaceContainerHigh) {
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
                text = selectedName?.let { "Sélectionné : $it" } ?: "Sélectionnez un locataire.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Astuce : utilisez Entrée pour ouvrir la fiche.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
