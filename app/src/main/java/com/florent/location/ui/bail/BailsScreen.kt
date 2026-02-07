package com.florent.location.ui.bail

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
import com.florent.location.ui.components.AdaptiveContent
import com.florent.location.ui.components.ExpressiveEmptyState
import com.florent.location.ui.components.ExpressiveErrorState
import com.florent.location.ui.components.ExpressiveLoadingState
import com.florent.location.ui.components.LeaseCard
import com.florent.location.ui.components.SectionHeader
import com.florent.location.ui.components.windowWidthSize
import com.florent.location.ui.components.WindowWidthSize
import org.koin.androidx.compose.koinViewModel

@ExperimentalMaterial3Api
@Composable
fun BailsScreen(
    viewModel: BailsViewModel = koinViewModel(),
    onBailClick: (Long) -> Unit,
    onAddBail: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    BailsContent(
        state = state,
        onBailClick = onBailClick,
        onAddBail = onAddBail,
        modifier = modifier
    )
}

@ExperimentalMaterial3Api
@Composable
private fun BailsContent(
    state: BailsUiState,
    onBailClick: (Long) -> Unit,
    onAddBail: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(text = "Baux") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddBail, modifier = Modifier.focusable()) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Ajouter un bail")
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
                            title = "Chargement des baux",
                            message = "Nous préparons vos contrats et les échéances clés."
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
                            title = "Aucun bail enregistré",
                            message = "Créez votre premier bail pour suivre les loyers et indexations.",
                            icon = Icons.Outlined.Inbox,
                            actionLabel = "Créer un bail",
                            onAction = onAddBail
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
                                    items(state.bails, key = { it.id }) { bail ->
                                        LeaseCard(
                                            bail = bail,
                                            onOpen = { onBailClick(bail.id) },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                                BailsContextPanel(
                                    total = state.bails.size,
                                    active = state.bails.count { it.endDateEpochDay == null },
                                    modifier = Modifier.weight(0.42f)
                                )
                            }
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(state.bails, key = { it.id }) { bail ->
                                    LeaseCard(
                                        bail = bail,
                                        onOpen = { onBailClick(bail.id) },
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
private fun BailsContextPanel(
    total: Int,
    active: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionHeader(
            title = "Aperçu",
            supportingText = "Gardez un œil sur l'état des baux."
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
                    text = "Consultez chaque bail pour suivre les échéances clés.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
