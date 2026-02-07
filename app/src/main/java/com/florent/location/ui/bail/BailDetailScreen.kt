@file:OptIn(ExperimentalMaterial3Api::class)

package com.florent.location.ui.bail

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ReportProblem
import androidx.compose.material.icons.outlined.Timelapse
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key as KeyboardKey
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import com.florent.location.ui.components.ExpressiveEmptyState
import com.florent.location.ui.components.ExpressiveErrorState
import com.florent.location.ui.components.ExpressiveLoadingState
import com.florent.location.ui.components.CardVariant
import com.florent.location.ui.components.DestructiveActionCard
import com.florent.location.ui.components.DateFieldWithPicker
import com.florent.location.ui.components.HeroSummaryCard
import com.florent.location.ui.components.LabeledValueRow
import com.florent.location.ui.components.NonInteractiveBadge
import com.florent.location.ui.components.NonInteractiveChip
import com.florent.location.ui.components.PrimaryActionRow
import com.florent.location.ui.components.ResultCard
import com.florent.location.ui.components.ScreenScaffold
import com.florent.location.ui.components.SectionCard
import com.florent.location.ui.components.SectionHeader
import com.florent.location.ui.components.TimelineListItem
import com.florent.location.ui.components.UiTokens
import com.florent.location.ui.components.formatCurrency
import com.florent.location.ui.components.formatEpochDay
import com.florent.location.ui.components.windowWidthSize
import com.florent.location.ui.components.WindowWidthSize
import com.florent.location.domain.model.Bail
import com.florent.location.domain.model.Key as BailKey

@Composable
fun BailDetailScreen(
    viewModel: BailDetailViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    BailDetailContent(
        state = state,
        onEvent = viewModel::onEvent,
        modifier = modifier
    )
}

@Composable
private fun BailDetailContent(
    state: BailDetailUiState,
    onEvent: (BailDetailUiEvent) -> Unit,
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
                        message = "Nous préparons le résumé, les clés et l'indexation."
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
                        icon = Icons.Outlined.ReportProblem
                    )
                }
            }

            state.bail == null -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ExpressiveEmptyState(
                        title = "Bail introuvable",
                        message = "Ce bail n'est plus disponible.",
                        icon = Icons.Outlined.Inbox
                    )
                }
            }

            else -> {
                val bail = state.bail
                BoxWithConstraints {
                    val sizeClass = windowWidthSize(maxWidth)
                    val scrollState = rememberScrollState()
                    if (sizeClass == WindowWidthSize.Expanded) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(scrollState),
                            horizontalArrangement = Arrangement.spacedBy(UiTokens.SpacingXL),
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(
                                modifier = Modifier.weight(0.55f),
                                verticalArrangement = Arrangement.spacedBy(UiTokens.SpacingL)
                            ) {
                                BailSummarySection(bail = bail, isActive = state.isActive)
                                KeysSection(
                                    keys = state.keys,
                                    onAddKey = { onEvent(BailDetailUiEvent.AddKeyClicked) },
                                    onDeleteKey = { keyId ->
                                        onEvent(BailDetailUiEvent.DeleteKeyClicked(keyId))
                                    }
                                )
                                DestructiveActionsSection(
                                    isActive = state.isActive,
                                    onCloseLease = { onEvent(BailDetailUiEvent.CloseLeaseClicked) }
                                )
                            }
                            Column(
                                modifier = Modifier.weight(0.45f),
                                verticalArrangement = Arrangement.spacedBy(UiTokens.SpacingL)
                            ) {
                                IndexationSection(
                                    state = state,
                                    onEvent = onEvent
                                )
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(scrollState),
                            verticalArrangement = Arrangement.spacedBy(UiTokens.SpacingL)
                        ) {
                            BailSummarySection(bail = bail, isActive = state.isActive)
                            IndexationSection(state = state, onEvent = onEvent)
                            KeysSection(
                                keys = state.keys,
                                onAddKey = { onEvent(BailDetailUiEvent.AddKeyClicked) },
                                onDeleteKey = { keyId ->
                                    onEvent(BailDetailUiEvent.DeleteKeyClicked(keyId))
                                }
                            )
                            DestructiveActionsSection(
                                isActive = state.isActive,
                                onCloseLease = { onEvent(BailDetailUiEvent.CloseLeaseClicked) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (state.addKeyDialog.isOpen) {
        AlertDialog(
            onDismissRequest = { onEvent(BailDetailUiEvent.DismissAddKeyDialog) },
            modifier = Modifier.onPreviewKeyEvent {
                if (it.type == KeyEventType.KeyUp && it.key == KeyboardKey.Escape) {
                    onEvent(BailDetailUiEvent.DismissAddKeyDialog)
                    true
                } else if (it.type == KeyEventType.KeyUp &&
                    (it.key == KeyboardKey.Enter || it.key == KeyboardKey.NumPadEnter)
                ) {
                    onEvent(
                        BailDetailUiEvent.ConfirmAddKey(
                            type = state.addKeyDialog.type,
                            deviceLabel = state.addKeyDialog.deviceLabel,
                            handedOverDate = state.addKeyDialog.handedOverDate
                        )
                    )
                    true
                } else {
                    false
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onEvent(
                            BailDetailUiEvent.ConfirmAddKey(
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
                TextButton(onClick = { onEvent(BailDetailUiEvent.DismissAddKeyDialog) }) {
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
                            onEvent(BailDetailUiEvent.AddKeyFieldChanged(AddKeyField.Type, it))
                        },
                        label = { Text(text = "Type") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    androidx.compose.material3.OutlinedTextField(
                        value = state.addKeyDialog.deviceLabel,
                        onValueChange = {
                            onEvent(BailDetailUiEvent.AddKeyFieldChanged(AddKeyField.DeviceLabel, it))
                        },
                        label = { Text(text = "Étiquette dispositif") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DateFieldWithPicker(
                        label = "Date de remise",
                        value = state.addKeyDialog.handedOverDate,
                        onValueChange = {
                            onEvent(BailDetailUiEvent.AddKeyFieldChanged(AddKeyField.HandedOverDate, it))
                        }
                    )
                }
            }
        )
    }

    if (state.closeLeaseDialog.isOpen) {
        AlertDialog(
            onDismissRequest = { onEvent(BailDetailUiEvent.DismissCloseLeaseDialog) },
            modifier = Modifier.onPreviewKeyEvent {
                if (it.type == KeyEventType.KeyUp && it.key == KeyboardKey.Escape) {
                    onEvent(BailDetailUiEvent.DismissCloseLeaseDialog)
                    true
                } else if (it.type == KeyEventType.KeyUp &&
                    (it.key == KeyboardKey.Enter || it.key == KeyboardKey.NumPadEnter)
                ) {
                    onEvent(BailDetailUiEvent.ConfirmCloseLease(state.closeLeaseDialog.endDate))
                    true
                } else {
                    false
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onEvent(BailDetailUiEvent.ConfirmCloseLease(state.closeLeaseDialog.endDate))
                    }
                ) {
                    Text(text = "Clôturer")
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(BailDetailUiEvent.DismissCloseLeaseDialog) }) {
                    Text(text = "Annuler")
                }
            },
            title = { Text(text = "Clôturer le bail") },
            text = {
                DateFieldWithPicker(
                    label = "Date de fin",
                    value = state.closeLeaseDialog.endDate,
                    onValueChange = { onEvent(BailDetailUiEvent.CloseLeaseFieldChanged(it)) }
                )
            }
        )
    }
}

@Composable
private fun BailSummarySection(
    bail: Bail,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val statusLabel = if (isActive) "Actif" else "Terminé"
    val variant = if (isActive) CardVariant.Highlighted else CardVariant.Default
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(UiTokens.SpacingS)
    ) {
        HeroSummaryCard(
            title = "Bail #${bail.id}",
            status = statusLabel,
            heroValue = formatCurrency(bail.rentCents),
            heroLabel = "/ mois",
            facts = listOf(
                "Charges" to formatCurrency(bail.chargesCents),
                "Caution" to formatCurrency(bail.depositCents),
                "Échéance" to "Jour ${bail.rentDueDayOfMonth}",
                "Début" to formatEpochDay(bail.startDateEpochDay)
            ),
            variant = variant
        )
        SectionHeader(title = "Résumé")
        SectionCard {
            LabeledValueRow(
                label = "Fin",
                value = bail.endDateEpochDay?.let { formatEpochDay(it) } ?: "Actif"
            )
            NonInteractiveChip(
                label = "Logement #${bail.housingId}",
                icon = Icons.Outlined.Home
            )
            NonInteractiveChip(
                label = "Locataire #${bail.tenantId}",
                icon = Icons.Outlined.Person
            )
            if (
                bail.mailboxLabel != null ||
                bail.meterGas != null ||
                bail.meterElectricity != null ||
                bail.meterWater != null
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(UiTokens.SpacingS)) {
                    bail.mailboxLabel?.let {
                        NonInteractiveChip(
                            label = "Boîte $it",
                            icon = Icons.Outlined.Inbox
                        )
                    }
                    bail.meterGas?.let {
                        NonInteractiveChip(
                            label = "Gaz $it",
                            icon = Icons.Outlined.Payments
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(UiTokens.SpacingS)) {
                    bail.meterElectricity?.let {
                        NonInteractiveChip(
                            label = "Élec. $it",
                            icon = Icons.Outlined.Payments
                        )
                    }
                    bail.meterWater?.let {
                        NonInteractiveChip(
                            label = "Eau $it",
                            icon = Icons.Outlined.Payments
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun IndexationSection(
    state: BailDetailUiState,
    onEvent: (BailDetailUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(UiTokens.SpacingS)
    ) {
        SectionHeader(title = "Indexation")
        SectionCard(tonalColor = MaterialTheme.colorScheme.surfaceContainerHigh) {
            state.indexationPolicy?.let { policy ->
                Text(
                    text = policy.ruleLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(UiTokens.SpacingS)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.EventAvailable,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(text = "Prochaine échéance", style = MaterialTheme.typography.titleMedium)
                }
                NonInteractiveBadge(label = formatEpochDay(policy.nextIndexationEpochDay))
                LabeledValueRow(
                    label = "Anniversaire",
                    value = formatEpochDay(policy.anniversaryEpochDay)
                )
            }
        }
        SectionCard {
            Text(text = "Simulation", style = MaterialTheme.typography.titleMedium)
            androidx.compose.material3.OutlinedTextField(
                value = state.indexationForm.indexPercent,
                onValueChange = { onEvent(BailDetailUiEvent.IndexationPercentChanged(it)) },
                label = { Text(text = "Indice (%)") },
                modifier = Modifier.fillMaxWidth()
            )
            DateFieldWithPicker(
                label = "Date d'effet",
                value = state.indexationForm.effectiveDate,
                onValueChange = { onEvent(BailDetailUiEvent.IndexationEffectiveDateChanged(it)) }
            )
            PrimaryActionRow(
                primaryLabel = "Simuler",
                onPrimary = { onEvent(BailDetailUiEvent.SimulateIndexation) },
                secondaryLabel = "Appliquer",
                onSecondary = { onEvent(BailDetailUiEvent.ApplyIndexation) }
            )
        }
        state.indexationForm.simulation?.let { simulation ->
            ResultCard(
                title = "Résultat",
                entries = listOf(
                    "Loyer actuel" to formatCurrency(simulation.baseRentCents),
                    "Nouveau loyer" to formatCurrency(simulation.newRentCents),
                    "Date d'effet" to formatEpochDay(simulation.effectiveEpochDay)
                )
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(UiTokens.SpacingS)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(UiTokens.SpacingS)
            ) {
                Icon(
                    imageVector = Icons.Outlined.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Historique",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            if (state.indexationHistory.isEmpty()) {
                Text(
                    text = "Aucune indexation appliquée.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                state.indexationHistory.forEach { event ->
                    TimelineListItem(
                        title = formatEpochDay(event.appliedEpochDay),
                        subtitle = "${formatCurrency(event.baseRentCents)} → " +
                            formatCurrency(event.newRentCents),
                        trailing = "Indice ${event.indexPercent}%"
                    )
                }
            }
        }
    }
}

@Composable
private fun KeysSection(
    keys: List<BailKey>,
    onAddKey: () -> Unit,
    onDeleteKey: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(UiTokens.SpacingS)
    ) {
        SectionHeader(title = "Clés")
        if (keys.isEmpty()) {
            SectionCard {
                Text(
                    text = "Aucune clé enregistrée.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Ajoutez la première clé remise au locataire.",
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
                            key.deviceLabel?.let {
                                NonInteractiveBadge(label = it)
                            }
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

@Composable
private fun DestructiveActionsSection(
    isActive: Boolean,
    onCloseLease: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isActive) return
    DestructiveActionCard(
        title = "Clôturer le bail",
        message = "Marquez le bail comme terminé.",
        actionLabel = "Clôturer",
        onAction = onCloseLease,
        icon = Icons.Outlined.ReportProblem,
        modifier = modifier
    )
}
