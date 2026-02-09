package com.florent.location.ui.housing

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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import com.florent.location.ui.components.ExpressiveEmptyState
import com.florent.location.ui.components.ExpressiveErrorState
import com.florent.location.ui.components.ExpressiveLoadingState
import com.florent.location.ui.components.HousingCard
import com.florent.location.ui.components.SectionHeader
import com.florent.location.ui.components.ScreenScaffold
import com.florent.location.ui.components.SectionCard
import com.florent.location.ui.components.UiTokens
import com.florent.location.ui.components.windowWidthSize
import com.florent.location.ui.components.WindowWidthSize
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.HomeWork
import androidx.compose.material.icons.outlined.ListAlt
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.IconButton
import org.koin.androidx.compose.koinViewModel

@ExperimentalMaterial3Api
@Composable
fun HousingListScreen(
    viewModel: HousingListViewModel = koinViewModel(),
    onHousingClick: (Long) -> Unit,
    onAddHousing: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    HousingListContent(
        state = state,
        onEvent = viewModel::onEvent,
        onHousingClick = onHousingClick,
        onAddHousing = onAddHousing,
        modifier = modifier
    )
}

@ExperimentalMaterial3Api
@Composable
private fun HousingListContent(
    state: HousingListUiState,
    onEvent: (HousingListUiEvent) -> Unit,
    onHousingClick: (Long) -> Unit,
    onAddHousing: () -> Unit,
    modifier: Modifier = Modifier
) {
    ScreenScaffold(
        title = "Logements",
        floatingActionButton = {
            FloatingActionButton(onClick = onAddHousing, modifier = Modifier.focusable()) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Ajouter un logement")
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
                        title = "Chargement des logements",
                        message = "Nous préparons vos biens et leurs informations clés."
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
                        title = "Impossible de charger les logements",
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
                        title = "Aucun logement",
                        message = "Ajoutez un premier bien pour suivre loyers et baux.",
                        icon = Icons.Outlined.HomeWork,
                        actionLabel = "Ajouter un logement",
                        onAction = onAddHousing
                    )
                }
            }

            else -> {
                BoxWithConstraints {
                    val sizeClass = windowWidthSize(maxWidth)
                    val searchField: @Composable () -> Unit = {
                        SectionCard {
                            OutlinedTextField(
                                value = state.searchQuery,
                                onValueChange = { onEvent(HousingListUiEvent.SearchQueryChanged(it)) },
                                label = { Text(text = "Rechercher") },
                                modifier = Modifier.fillMaxWidth(),
                                trailingIcon = {
                                    if (state.searchQuery.isNotBlank()) {
                                        IconButton(
                                            onClick = { onEvent(HousingListUiEvent.SearchQueryChanged("")) }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Close,
                                                contentDescription = "Effacer la recherche"
                                            )
                                        }
                                    }
                                }
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
                                    items(state.housings, key = { it.housing.id }) { item ->
                                        HousingCard(
                                            housing = item.housing,
                                            situation = item.situation,
                                            onOpen = { onHousingClick(item.housing.id) },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                            HousingContextPanel(
                                total = state.housings.size,
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
                                items(state.housings, key = { it.housing.id }) { item ->
                                    HousingCard(
                                        housing = item.housing,
                                        situation = item.situation,
                                        onOpen = { onHousingClick(item.housing.id) },
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
private fun HousingContextPanel(
    total: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(UiTokens.SpacingL)
    ) {
        SectionHeader(
            title = "Conseils & raccourcis",
            supportingText = "Gérez vos biens rapidement."
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
                    text = "Logements au total : $total",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Text(
                text = "Sélectionnez un logement pour voir les baux associés.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Créer un logement depuis l'action principale.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
