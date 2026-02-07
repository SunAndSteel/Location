@file:OptIn(ExperimentalMaterial3Api::class)

package com.florent.location.ui.bail

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.florent.location.ui.components.AdaptiveContent
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
    Scaffold(
        topBar = { TopAppBar(title = { Text(text = "Détail du bail") }) },
        modifier = modifier
    ) { innerPadding ->
        AdaptiveContent(innerPadding = innerPadding) {
            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Chargement du bail...")
                        }
                    }
                }

                state.errorMessage != null -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.errorMessage,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "Veuillez réessayer plus tard.")
                    }
                }

                state.bail == null -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Bail introuvable.")
                    }
                }

                else -> {
                    val bail = state.bail
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "Résumé du bail",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.semantics { heading() }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = "Logement: ${bail.housingId}")
                                Text(text = "Locataire: ${bail.tenantId}")
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = "Loyer courant: ${bail.rentCents} cents")
                                Text(text = "Charges: ${bail.chargesCents} cents")
                                Text(text = "Caution: ${bail.depositCents} cents")
                                Text(text = "Échéance: ${bail.rentDueDayOfMonth}")
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = "Début: ${formatEpochDay(bail.startDateEpochDay)}")
                                Text(
                                    text = "Fin: ${bail.endDateEpochDay?.let { formatEpochDay(it) } ?: "Actif"}"
                                )
                                bail.mailboxLabel?.let { Text(text = "Boîte aux lettres: $it") }
                                bail.meterGas?.let { Text(text = "Compteur gaz: $it") }
                                bail.meterElectricity?.let { Text(text = "Compteur électricité: $it") }
                                bail.meterWater?.let { Text(text = "Compteur eau: $it") }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Indexation",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.semantics { heading() }
                        )
                        state.indexationPolicy?.let { policy ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = policy.ruleLabel)
                            Text(text = "Anniversaire: ${formatEpochDay(policy.anniversaryEpochDay)}")
                            Text(text = "Prochaine échéance: ${formatEpochDay(policy.nextIndexationEpochDay)}")
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "Simulation", style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = state.indexationForm.indexPercent,
                            onValueChange = { onEvent(BailDetailUiEvent.IndexationPercentChanged(it)) },
                            label = { Text(text = "Indice (%)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = state.indexationForm.effectiveDate,
                            onValueChange = { onEvent(BailDetailUiEvent.IndexationEffectiveDateChanged(it)) },
                            label = { Text(text = "Date d'effet (yyyy-MM-dd)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { onEvent(BailDetailUiEvent.SimulateIndexation) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = "Simuler")
                            }
                            Button(
                                onClick = { onEvent(BailDetailUiEvent.ApplyIndexation) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = "Appliquer")
                            }
                        }
                        state.indexationForm.simulation?.let { simulation ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Loyer actuel: ${simulation.baseRentCents} cents")
                            Text(text = "Nouveau loyer: ${simulation.newRentCents} cents")
                            Text(text = "Date d'effet: ${formatEpochDay(simulation.effectiveEpochDay)}")
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "Historique", style = MaterialTheme.typography.titleSmall)
                        if (state.indexationHistory.isEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Aucune indexation appliquée.")
                        } else {
                            state.indexationHistory.forEach { event ->
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Le ${formatEpochDay(event.appliedEpochDay)}: " +
                                        "${event.baseRentCents} → ${event.newRentCents} cents"
                                )
                                Text(text = "Indice: ${event.indexPercent}%")
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Clés",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.semantics { heading() }
                        )

                        if (state.keys.isEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Aucune clé enregistrée.")
                        } else {
                            state.keys.forEach { key ->
                                Spacer(modifier = Modifier.height(8.dp))
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(text = "Type: ${key.type}")
                                        key.deviceLabel?.let { Text(text = "Dispositif: $it") }
                                        Text(text = "Remise: ${formatEpochDay(key.handedOverEpochDay)}")
                                        TextButton(
                                            onClick = { onEvent(BailDetailUiEvent.DeleteKeyClicked(key.id)) },
                                            modifier = Modifier.focusable()
                                        ) {
                                            Text(text = "Supprimer la clé")
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { onEvent(BailDetailUiEvent.AddKeyClicked) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = "Ajouter une clé")
                            }
                            if (state.isActive) {
                                Button(
                                    onClick = { onEvent(BailDetailUiEvent.CloseLeaseClicked) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(text = "Clôturer le bail")
                                }
                            }
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
                if (it.type == KeyEventType.KeyUp && it.key == Key.Escape) {
                    onEvent(BailDetailUiEvent.DismissAddKeyDialog)
                    true
                } else if (it.type == KeyEventType.KeyUp &&
                    (it.key == Key.Enter || it.key == Key.NumPadEnter)
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
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = state.addKeyDialog.type,
                        onValueChange = {
                            onEvent(BailDetailUiEvent.AddKeyFieldChanged(AddKeyField.Type, it))
                        },
                        label = { Text(text = "Type") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.addKeyDialog.deviceLabel,
                        onValueChange = {
                            onEvent(BailDetailUiEvent.AddKeyFieldChanged(AddKeyField.DeviceLabel, it))
                        },
                        label = { Text(text = "Étiquette dispositif") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.addKeyDialog.handedOverDate,
                        onValueChange = {
                            onEvent(BailDetailUiEvent.AddKeyFieldChanged(AddKeyField.HandedOverDate, it))
                        },
                        label = { Text(text = "Remise (yyyy-MM-dd)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        )
    }

    if (state.closeLeaseDialog.isOpen) {
        AlertDialog(
            onDismissRequest = { onEvent(BailDetailUiEvent.DismissCloseLeaseDialog) },
            modifier = Modifier.onPreviewKeyEvent {
                if (it.type == KeyEventType.KeyUp && it.key == Key.Escape) {
                    onEvent(BailDetailUiEvent.DismissCloseLeaseDialog)
                    true
                } else if (it.type == KeyEventType.KeyUp &&
                    (it.key == Key.Enter || it.key == Key.NumPadEnter)
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
                OutlinedTextField(
                    value = state.closeLeaseDialog.endDate,
                    onValueChange = { onEvent(BailDetailUiEvent.CloseLeaseFieldChanged(it)) },
                    label = { Text(text = "Date de fin (yyyy-MM-dd)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        )
    }
}

private fun formatEpochDay(epochDay: Long): String {
    return LocalDate.ofEpochDay(epochDay).format(DateTimeFormatter.ISO_LOCAL_DATE)
}
