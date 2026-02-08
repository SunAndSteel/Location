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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.florent.location.ui.components.AppSectionHeader
import com.florent.location.ui.components.CardVariant
import com.florent.location.ui.components.DestructiveActionCard
import com.florent.location.ui.components.ExpressiveEmptyState
import com.florent.location.ui.components.ExpressiveErrorState
import com.florent.location.ui.components.ExpressiveLoadingState
import com.florent.location.ui.components.HeroSummaryCard
import com.florent.location.ui.components.LabeledValueRow
import com.florent.location.ui.components.NonInteractiveChip
import com.florent.location.ui.components.PrimaryActionRow
import com.florent.location.ui.components.ScreenScaffold
import com.florent.location.ui.components.SectionCard
import com.florent.location.ui.components.UiTokens
import com.florent.location.ui.components.housingSituationLabel
import com.florent.location.ui.components.formatCurrency
import com.florent.location.ui.components.windowWidthSize
import com.florent.location.ui.components.WindowWidthSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.HomeWork
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
    ScreenScaffold(
        title = "Logement",
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
                val situation = state.situation
                if (housing != null && situation != null) {
                    BoxWithConstraints {
                        val sizeClass = windowWidthSize(maxWidth)
                        if (sizeClass == WindowWidthSize.Expanded) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(UiTokens.SpacingXL)
                            ) {
                                Column(
                                    modifier = Modifier.weight(0.6f),
                                    verticalArrangement = Arrangement.spacedBy(UiTokens.SpacingL)
                                ) {
                                    HousingHeroSection(housing = housing, situationLabel = housingSituationLabel(situation))
                                    HousingInfoSection(housing = housing)
                                    HousingAccessSection(housing = housing)
                                    HousingFinancialSection(housing = housing)
                                }
                                Column(
                                    modifier = Modifier.weight(0.4f),
                                    verticalArrangement = Arrangement.spacedBy(UiTokens.SpacingL)
                                ) {
                                    AppSectionHeader(title = "Actions")
                                    PrimaryActionRow(
                                        primaryLabel = "Modifier",
                                        onPrimary = onEdit,
                                        secondaryLabel = "Créer un bail",
                                        onSecondary = onCreateLease
                                    )
                                    AppSectionHeader(title = "Zone dangereuse")
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
                            Column(verticalArrangement = Arrangement.spacedBy(UiTokens.SpacingL)) {
                                HousingHeroSection(housing = housing, situationLabel = housingSituationLabel(situation))
                                HousingInfoSection(housing = housing)
                                HousingAccessSection(housing = housing)
                                HousingFinancialSection(housing = housing)
                                AppSectionHeader(title = "Actions")
                                PrimaryActionRow(
                                    primaryLabel = "Modifier",
                                    onPrimary = onEdit,
                                    secondaryLabel = "Créer un bail",
                                    onSecondary = onCreateLease
                                )
                                AppSectionHeader(title = "Zone dangereuse")
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

@Composable
private fun HousingHeroSection(
    housing: Housing,
    situationLabel: String,
    modifier: Modifier = Modifier
) {
    HeroSummaryCard(
        title = "${housing.address}, ${housing.city}",
        heroValue = formatCurrency(housing.defaultRentCents),
        heroLabel = "/ mois",
        facts = listOf(
            "Statut" to situationLabel,
            "Charges" to formatCurrency(housing.defaultChargesCents),
            "Caution" to formatCurrency(housing.depositCents),
            "Ville" to housing.city
        ),
        variant = CardVariant.Highlighted,
        modifier = modifier
    )
}

@Composable
private fun HousingInfoSection(
    housing: Housing,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(UiTokens.SpacingS)
    ) {
        AppSectionHeader(
            title = "Informations",
            supportingText = "Détails complémentaires sur le bien."
        )
        SectionCard {
            LabeledValueRow(label = "Ville", value = housing.city)
            LabeledValueRow(label = "PEB", value = housing.peb ?: "Non renseigné")
            LabeledValueRow(label = "Bâtiment", value = housing.buildingLabel ?: "Non renseigné")
        }
    }
}

@Composable
private fun HousingFinancialSection(
    housing: Housing,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(UiTokens.SpacingS)
    ) {
        AppSectionHeader(
            title = "Conditions financières",
            supportingText = "Montants enregistrés pour ce logement."
        )
        SectionCard {
            LabeledValueRow(label = "Loyer", value = formatCurrency(housing.defaultRentCents))
            LabeledValueRow(label = "Charges", value = formatCurrency(housing.defaultChargesCents))
            LabeledValueRow(label = "Caution", value = formatCurrency(housing.depositCents))
        }
    }
}

@Composable
private fun HousingAccessSection(
    housing: Housing,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(UiTokens.SpacingS)
    ) {
        AppSectionHeader(
            title = "Accès et compteurs",
            supportingText = "Informations liées à la boîte aux lettres et aux compteurs."
        )
        SectionCard {
            LabeledValueRow(label = "Boîte aux lettres", value = housing.mailboxLabel ?: "Non renseigné")
            LabeledValueRow(label = "Compteur gaz", value = housing.meterGas ?: "Non renseigné")
            LabeledValueRow(label = "Compteur électricité", value = housing.meterElectricity ?: "Non renseigné")
            LabeledValueRow(label = "Compteur eau", value = housing.meterWater ?: "Non renseigné")
        }
    }
}
