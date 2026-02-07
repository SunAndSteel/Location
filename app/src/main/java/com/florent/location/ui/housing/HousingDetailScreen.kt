package com.florent.location.ui.housing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.florent.location.ui.components.AppSectionHeader
import com.florent.location.ui.components.CardVariant
import com.florent.location.ui.components.DestructiveActionCard
import com.florent.location.ui.components.ExpressiveEmptyState
import com.florent.location.ui.components.ExpressiveErrorState
import com.florent.location.ui.components.ExpressiveLoadingState
import com.florent.location.ui.components.HeroCard
import com.florent.location.ui.components.MetricChip
import com.florent.location.ui.components.PrimaryActionRow
import com.florent.location.ui.components.formatCurrency
import com.florent.location.ui.components.windowWidthSize
import com.florent.location.ui.components.WindowWidthSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.HomeWork
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.ReportProblem
import com.florent.location.domain.model.Housing

@Composable
fun HousingDetailScreen(
    viewModel: HousingDetailViewModel,
    onEdit: () -> Unit,
    onCreateLease: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    HousingDetailContent(
        state = state,
        onEvent = viewModel::onEvent,
        onEdit = onEdit,
        onCreateLease = onCreateLease,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HousingDetailContent(
    state: HousingDetailUiState,
    onEvent: (HousingDetailUiEvent) -> Unit,
    onEdit: () -> Unit,
    onCreateLease: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(text = "Détail du logement") }) },
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
                            title = "Chargement du logement",
                            message = "Nous préparons les informations du bien."
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
                            title = "Impossible de charger le logement",
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
                            title = "Logement introuvable",
                            message = "Ce logement n'est plus disponible.",
                            icon = Icons.Outlined.HomeWork
                        )
                    }
                }

                else -> {
                    val housing = state.housing
                    if (housing != null) {
                        BoxWithConstraints {
                            val sizeClass = windowWidthSize(maxWidth)
                            if (sizeClass == WindowWidthSize.Expanded) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.weight(0.6f),
                                        verticalArrangement = Arrangement.spacedBy(20.dp)
                                    ) {
                                        HeroCard(
                                            title = housing.address,
                                            statusBadge = "Statut inconnu",
                                            heroValue = formatCurrency(housing.defaultRentCents),
                                            heroLabel = "/ mois",
                                            variant = CardVariant.Highlighted,
                                            secondaryMetrics = listOf(
                                                "Ville" to housing.city,
                                                "Charges" to formatCurrency(housing.defaultChargesCents),
                                                "Caution" to formatCurrency(housing.depositCents)
                                            )
                                        )
                                        HousingInfoSection(housing = housing)
                                    }
                                    Column(
                                        modifier = Modifier.weight(0.4f),
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        PrimaryActionRow(
                                            primaryLabel = "Modifier",
                                            onPrimary = onEdit,
                                            secondaryLabel = "Créer un bail",
                                            onSecondary = onCreateLease
                                        )
                                        DestructiveActionCard(
                                            title = "Supprimer le logement",
                                            message = "Cette action est définitive.",
                                            actionLabel = "Supprimer",
                                            onAction = {
                                                onEvent(HousingDetailUiEvent.DeleteHousing(housing.id))
                                            },
                                            icon = Icons.Outlined.ReportProblem
                                        )
                                    }
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                                    HeroCard(
                                        title = housing.address,
                                        statusBadge = "Statut inconnu",
                                        heroValue = formatCurrency(housing.defaultRentCents),
                                        heroLabel = "/ mois",
                                        variant = CardVariant.Highlighted,
                                        secondaryMetrics = listOf(
                                            "Ville" to housing.city,
                                            "Charges" to formatCurrency(housing.defaultChargesCents),
                                            "Caution" to formatCurrency(housing.depositCents)
                                        )
                                    )
                                    HousingInfoSection(housing = housing)
                                    PrimaryActionRow(
                                        primaryLabel = "Modifier",
                                        onPrimary = onEdit,
                                        secondaryLabel = "Créer un bail",
                                        onSecondary = onCreateLease
                                    )
                                    DestructiveActionCard(
                                        title = "Supprimer le logement",
                                        message = "Cette action est définitive.",
                                        actionLabel = "Supprimer",
                                        onAction = {
                                            onEvent(HousingDetailUiEvent.DeleteHousing(housing.id))
                                        },
                                        icon = Icons.Outlined.ReportProblem
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
private fun HousingInfoSection(
    housing: Housing,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AppSectionHeader(
            title = "Informations",
            supportingText = "Détails complémentaires sur le bien."
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
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Caractéristiques",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                MetricChip(label = "Ville ${housing.city}")
                housing.peb?.let { MetricChip(label = "PEB $it") }
                housing.buildingLabel?.let { MetricChip(label = "Bâtiment $it") }
            }
        }
    }
}
