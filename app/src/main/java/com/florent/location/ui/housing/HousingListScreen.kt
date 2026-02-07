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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import com.florent.location.ui.components.AdaptiveContent
import com.florent.location.ui.components.ExpressiveEmptyState
import com.florent.location.ui.components.ExpressiveErrorState
import com.florent.location.ui.components.ExpressiveLoadingState
import com.florent.location.ui.components.HousingCard
import com.florent.location.ui.components.SectionHeader
import com.florent.location.ui.components.windowWidthSize
import com.florent.location.ui.components.WindowWidthSize
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.HomeWork
import androidx.compose.material.icons.outlined.ListAlt
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
    Scaffold(
        topBar = { TopAppBar(title = { Text(text = "Logements") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddHousing, modifier = Modifier.focusable()) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Ajouter un logement")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        AdaptiveContent(innerPadding = innerPadding, contentMaxWidth = 1080.dp) {
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
                            title = "Aucun logement enregistré",
                            message = "Ajoutez un premier bien pour suivre loyers et contrats.",
                            icon = Icons.Outlined.HomeWork,
                            actionLabel = "Ajouter un logement",
                            onAction = onAddHousing
                        )
                    }
                }

                else -> {
                    BoxWithConstraints {
                        val sizeClass = windowWidthSize(maxWidth)
                        if (sizeClass == WindowWidthSize.Expanded) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(24.dp)
                            ) {
                                LazyColumn(
                                    modifier = Modifier.weight(0.58f),
                                    contentPadding = PaddingValues(vertical = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    items(state.housings, key = { it.id }) { housing ->
                                        HousingCard(
                                            housing = housing,
                                            onOpen = { onHousingClick(housing.id) },
                                            onDelete = {
                                                onEvent(HousingListUiEvent.DeleteHousing(housing.id))
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                                HousingContextPanel(
                                    total = state.housings.size,
                                    modifier = Modifier.weight(0.42f)
                                )
                            }
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(state.housings, key = { it.id }) { housing ->
                                    HousingCard(
                                        housing = housing,
                                        onOpen = { onHousingClick(housing.id) },
                                        onDelete = {
                                            onEvent(HousingListUiEvent.DeleteHousing(housing.id))
                                        },
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
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionHeader(
            title = "Aperçu",
            supportingText = "Vision globale de vos logements."
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
                        text = "Logements au total : $total",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Text(
                    text = "Ouvrez un logement pour consulter ses baux.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
