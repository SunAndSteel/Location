package com.florent.location.ui.lease

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.ListAlt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.florent.location.ui.components.ExpressiveEmptyState
import com.florent.location.ui.components.ExpressiveErrorState
import com.florent.location.ui.components.ExpressiveLoadingState
import com.florent.location.ui.components.AppSearchBar
import com.florent.location.ui.components.LeaseCard
import com.florent.location.ui.components.ScreenScaffold
import com.florent.location.ui.components.SectionCard
import com.florent.location.ui.components.SectionHeader
import com.florent.location.ui.components.UiTokens
import com.florent.location.ui.components.windowWidthSize
import com.florent.location.ui.components.WindowWidthSize
import org.koin.androidx.compose.koinViewModel

@ExperimentalMaterial3Api
@Composable
fun LeaseListScreen(
    viewModel: LeaseListViewModel = koinViewModel(),
    onBailClick: (housingId: Long) -> Unit,
    onAddBail: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    LeaseListContent(
        state = state,
        onEvent = viewModel::onEvent,
        onBailClick = onBailClick,
        onAddBail = onAddBail,
        modifier = modifier
    )
}

@ExperimentalMaterial3Api
@Composable
private fun LeaseListContent(
    state: LeaseListUiState,
    onEvent: (LeaseListUiEvent) -> Unit,
    onBailClick: (housingId: Long) -> Unit,
    onAddBail: () -> Unit,
    modifier: Modifier = Modifier
) {
    ScreenScaffold(
        title = "Baux",
        floatingActionButton = {
            FloatingActionButton(onClick = onAddBail, modifier = Modifier.focusable()) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Ajouter un bail")
            }
        },
        contentMaxWidth = UiTokens.ContentMaxWidthExpanded,
        modifier = modifier
    ) {
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    ExpressiveLoadingState(
                        title = "Chargement des baux",
                        message = "Nous préparons vos baux et les échéances clés."
                    )
                }
            }

            state.errorMessage != null -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ExpressiveErrorState(
                        title = "Une erreur est survenue",
                        message = state.errorMessage,
                        icon = Icons.Outlined.ErrorOutline
                    )
                }
            }

            state.isEmpty -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ExpressiveEmptyState(
                        title = "Aucun bail",
                        message = "Créez votre premier bail pour suivre les loyers et indexations.",
                        icon = Icons.Outlined.Inbox,
                        actionLabel = "Créer un bail",
                        onAction = onAddBail
                    )
                }
            }


            state.isSearchResultEmpty -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ExpressiveEmptyState(
                        title = "Aucun résultat",
                        message = "Aucun bail ne correspond à \"${state.searchQuery}\".",
                        icon = Icons.Outlined.ListAlt,
                        actionLabel = "Effacer la recherche",
                        onAction = { onEvent(LeaseListUiEvent.SearchQueryChanged("")) }
                    )
                }
            }

            else -> {
                BoxWithConstraints {
                    val sizeClass = windowWidthSize(maxWidth)
                    val searchField: @Composable () -> Unit = {
                        SectionCard {
                            AppSearchBar(
                                query = state.searchQuery,
                                onQueryChange = { onEvent(LeaseListUiEvent.SearchQueryChanged(it)) },
                                placeholder = "Rechercher un bail"
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
                                LazyColumn(
                                    contentPadding = PaddingValues(vertical = UiTokens.SpacingS),
                                    verticalArrangement = Arrangement.spacedBy(UiTokens.SpacingL)
                                ) {
                                    items(state.bails, key = { it.bail.id }) { item ->
                                        LeaseCard(
                                            bail = item.bail,
                                            housingLabel = item.housingLabel,
                                            tenantLabel = item.tenantName,
                                            onOpen = { onBailClick(item.bail.housingId) },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                            LeaseContextPanel(
                                total = state.bails.size,
                                active = state.bails.count { it.bail.endDateEpochDay == null },
                                modifier = Modifier.weight(0.6f)
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(UiTokens.SpacingL)) {
                            searchField()
                            LazyColumn(
                                contentPadding = PaddingValues(vertical = UiTokens.SpacingS),
                                verticalArrangement = Arrangement.spacedBy(UiTokens.SpacingL)
                            ) {
                                items(state.bails, key = { it.bail.id }) { item ->
                                    LeaseCard(
                                        bail = item.bail,
                                        housingLabel = item.housingLabel,
                                        tenantLabel = item.tenantName,
                                        onOpen = { onBailClick(item.bail.housingId) },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LeaseContextPanel(
    total: Int,
    active: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(UiTokens.SpacingL)
    ) {
        SectionHeader(
            title = "Conseils & raccourcis",
            supportingText = "Suivez l'état des baux rapidement."
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
                    text = "Baux au total : $total",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Text(
                text = "Baux actifs : $active",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Astuce : utilisez Entrée pour ouvrir un bail.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
