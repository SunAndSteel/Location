@file:OptIn(ExperimentalMaterial3Api::class)

package com.florent.location.ui.lease

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.florent.location.ui.components.AppSectionHeader
import com.florent.location.ui.components.CardVariant
import com.florent.location.ui.components.DestructiveActionCard
import com.florent.location.ui.components.DateFieldWithPicker
import com.florent.location.ui.components.ExpressiveEmptyState
import com.florent.location.ui.components.ExpressiveErrorState
import com.florent.location.ui.components.ExpressiveLoadingState
import com.florent.location.ui.components.HeroSummaryCard
import com.florent.location.ui.components.LabeledValueRow
import com.florent.location.ui.components.NonInteractiveBadge
import com.florent.location.ui.components.ScreenScaffold
import com.florent.location.ui.components.SectionCard
import com.florent.location.ui.components.UiTokens
import com.florent.location.ui.components.PrimaryActionRow
import com.florent.location.ui.components.formatCurrency
import com.florent.location.ui.components.formatEpochDay
import com.florent.location.ui.components.windowWidthSize
import com.florent.location.ui.components.WindowWidthSize
import com.florent.location.domain.model.Key
import com.florent.location.domain.model.Lease
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.ReportProblem
import androidx.compose.material.icons.outlined.Schedule

@Composable
fun LeaseDetailScreen(
    viewModel: LeaseDetailViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    LeaseDetailContent(
        state = state,
        onEvent = viewModel::onEvent,
        modifier = modifier
    )
}

@Composable
private fun LeaseDetailContent(
    state: LeaseDetailUiState,
    onEvent: (LeaseDetailUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    ScreenScaffold(
        title = "Bail",
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
                        title = "Chargement du bail",
                        message = "Nous préparons les informations du bail."
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
                        title = "Impossible de charger le bail",
                        message = state.errorMessage,
                        icon = Icons.Outlined.ErrorOutline
                    )
                }
            }

            state.lease == null -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ExpressiveEmptyState(
                        title = "Bail introuvable",
                        message = "Ce bail n'est plus disponible.",
                        icon = Icons.Outlined.Schedule
                    )
                }
            }

            else -> {
                val lease = state.lease
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
                                LeaseSummarySection(lease = lease, isActive = state.isActive)
                                LeaseKeysSection(
                                    keys = state.keys,
                                    onAddKey = { onEvent(LeaseDetailUiEvent.AddKeyClicked) },
                                    onDeleteKey = { keyId ->
                                        onEvent(LeaseDetailUiEvent.DeleteKeyClicked(keyId))
                                    }
                                )
                                LeaseDatesSection(lease = lease)
                            }
                            Column(
                                modifier = Modifier.weight(0.4f),
                                verticalArrangement = Arrangement.spacedBy(UiTokens.SpacingL)
                            ) {
                                AppSectionHeader(title = "Actions")
                                PrimaryActionRow(
                                    primaryLabel = "Ajouter une clé",
                                    onPrimary = { onEvent(LeaseDetailUiEvent.AddKeyClicked) },
                                    secondaryLabel = if (state.isActive) "Clôturer le bail" else null,
                                    onSecondary = if (state.isActive) {
                                        { onEvent(LeaseDetailUiEvent.CloseLeaseClicked) }
                                    } else {
                                        null
                                    }
                                )
                                if (state.isActive) {
                                    AppSectionHeader(title = "Zone dangereuse")
                                    DestructiveActionCard(
                                        title = "Clôturer le bail",
                                        message = "Marquez le bail comme terminé.",
                                        actionLabel = "Clôturer",
                                        onAction = { onEvent(LeaseDetailUiEvent.CloseLeaseClicked) },
                                        icon = Icons.Outlined.ReportProblem
                                    )
                                }
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(UiTokens.SpacingL)) {
                            LeaseSummarySection(lease = lease, isActive = state.isActive)
                            LeaseKeysSection(
                                keys = state.keys,
                                onAddKey = { onEvent(LeaseDetailUiEvent.AddKeyClicked) },
                                onDeleteKey = { keyId ->
                                    onEvent(LeaseDetailUiEvent.DeleteKeyClicked(keyId))
                                }
                            )
                            LeaseDatesSection(lease = lease)
                            AppSectionHeader(title = "Actions")
                            PrimaryActionRow(
                                primaryLabel = "Ajouter une clé",
                                onPrimary = { onEvent(LeaseDetailUiEvent.AddKeyClicked) },
                                secondaryLabel = if (state.isActive) "Clôturer le bail" else null,
                                onSecondary = if (state.isActive) {
                                    { onEvent(LeaseDetailUiEvent.CloseLeaseClicked) }
                                } else {
                                    null
                                }
                            )
                            if (state.isActive) {
                                AppSectionHeader(title = "Zone dangereuse")
                                DestructiveActionCard(
                                    title = "Clôturer le bail",
                                    message = "Marquez le bail comme terminé.",
                                    actionLabel = "Clôturer",
                                    onAction = { onEvent(LeaseDetailUiEvent.CloseLeaseClicked) },
                                    icon = Icons.Outlined.ReportProblem
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (state.addKeyDialog.isOpen) {
        AlertDialog(
            onDismissRequest = { onEvent(LeaseDetailUiEvent.DismissAddKeyDialog) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onEvent(
                            LeaseDetailUiEvent.ConfirmAddKey(
                                type = state.addKeyDialog.type,
                                deviceLabel = state.addKeyDialog.deviceLabel,
                                handedOverDate = state.addKeyDialog.handedOverDate
                            )
                        )
                    }
                ) {
                    Text(text = "Ajouter")
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(LeaseDetailUiEvent.DismissAddKeyDialog) }) {
                    Text(text = "Annuler")
                }
            },
            title = { Text(text = "Ajouter une clé") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(UiTokens.SpacingS)
                ) {
                    androidx.compose.material3.OutlinedTextField(
                        value = state.addKeyDialog.type,
                        onValueChange = {
                            onEvent(LeaseDetailUiEvent.AddKeyFieldChanged(AddKeyField.Type, it))
                        },
                        label = { Text(text = "Type") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    androidx.compose.material3.OutlinedTextField(
                        value = state.addKeyDialog.deviceLabel,
                        onValueChange = {
                            onEvent(LeaseDetailUiEvent.AddKeyFieldChanged(AddKeyField.DeviceLabel, it))
                        },
                        label = { Text(text = "Étiquette dispositif") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DateFieldWithPicker(
                        label = "Date de remise",
                        value = state.addKeyDialog.handedOverDate,
                        onValueChange = {
                            onEvent(LeaseDetailUiEvent.AddKeyFieldChanged(AddKeyField.HandedOverDate, it))
                        }
                    )
                }
            }
        )
    }

    if (state.closeLeaseDialog.isOpen) {
        AlertDialog(
            onDismissRequest = { onEvent(LeaseDetailUiEvent.DismissCloseLeaseDialog) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onEvent(LeaseDetailUiEvent.ConfirmCloseLease(state.closeLeaseDialog.endDate))
                    }
                ) {
                    Text(text = "Clôturer")
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(LeaseDetailUiEvent.DismissCloseLeaseDialog) }) {
                    Text(text = "Annuler")
                }
            },
            title = { Text(text = "Clôturer le bail") },
            text = {
                DateFieldWithPicker(
                    label = "Date de fin",
                    value = state.closeLeaseDialog.endDate,
                    onValueChange = { onEvent(LeaseDetailUiEvent.CloseLeaseFieldChanged(it)) }
                )
            }
        )
    }
}

@Composable
private fun LeaseSummarySection(
    lease: Lease,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val status = if (isActive) "Actif" else "Terminé"
    HeroSummaryCard(
        title = "Bail #${lease.id}",
        status = status,
        heroValue = formatCurrency(lease.rentCents),
        heroLabel = "/ mois",
        variant = if (isActive) CardVariant.Highlighted else CardVariant.Default,
        facts = listOf(
            "Charges" to formatCurrency(lease.chargesCents),
            "Caution" to formatCurrency(lease.depositCents),
            "Échéance" to "Jour ${lease.rentDueDayOfMonth}",
            "Début" to formatEpochDay(lease.startDateEpochDay)
        ),
        modifier = modifier
    )
}

@Composable
private fun LeaseDatesSection(
    lease: Lease,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(UiTokens.SpacingS)
    ) {
        AppSectionHeader(
            title = "Dates",
            supportingText = "Début et fin du bail."
        )
        SectionCard {
            LabeledValueRow(
                label = "Début",
                value = formatEpochDay(lease.startDateEpochDay)
            )
            LabeledValueRow(
                label = "Fin",
                value = lease.endDateEpochDay?.let { formatEpochDay(it) } ?: "Actif"
            )
        }
    }
}

@Composable
private fun LeaseKeysSection(
    keys: List<Key>,
    onAddKey: () -> Unit,
    onDeleteKey: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(UiTokens.SpacingS)
    ) {
        AppSectionHeader(
            title = "Clés",
            supportingText = "Historique des clés remises."
        )
        if (keys.isEmpty()) {
            SectionCard {
                Text(
                    text = "Aucune clé enregistrée.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Ajoutez la première clé remise.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            keys.forEach { key ->
                SectionCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(UiTokens.SpacingXs)) {
                            Text(
                                text = key.type,
                                style = MaterialTheme.typography.titleMedium
                            )
                            key.deviceLabel?.let { NonInteractiveBadge(label = it) }
                            LabeledValueRow(
                                label = "Remise",
                                value = formatEpochDay(key.handedOverEpochDay)
                            )
                        }
                        TextButton(
                            onClick = { onDeleteKey(key.id) },
                            modifier = Modifier.focusable()
                        ) {
                            Text(text = "Supprimer")
                        }
                    }
                }
            }
        }
        PrimaryActionRow(
            primaryLabel = "Ajouter une clé",
            onPrimary = onAddKey
        )
    }
}
