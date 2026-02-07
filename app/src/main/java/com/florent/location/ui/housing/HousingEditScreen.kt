package com.florent.location.ui.housing

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.florent.location.ui.components.AdaptiveContent
import com.florent.location.ui.components.AppSectionHeader
import com.florent.location.ui.components.ExpressiveLoadingState
import com.florent.location.ui.components.PrimaryActionRow

@ExperimentalMaterial3Api
@Composable
fun HousingEditScreen(
    viewModel: HousingEditViewModel,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    HousingEditContent(
        state = state,
        onEvent = viewModel::onEvent,
        onSaved = onSaved,
        modifier = modifier
    )
}

@ExperimentalMaterial3Api
@Composable
private fun HousingEditContent(
    state: HousingEditUiState,
    onEvent: (HousingEditUiEvent) -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            onSaved()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (state.housingId == null) {
                            "Nouveau logement"
                        } else {
                            "Modifier le logement"
                        }
                    )
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        AdaptiveContent(innerPadding = innerPadding, contentMaxWidth = 960.dp) {
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    ExpressiveLoadingState(
                        title = "Chargement du logement",
                        message = "Nous préparons le formulaire."
                    )
                }
                return@AdaptiveContent
            }

            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AppSectionHeader(
                    title = "Informations",
                    supportingText = "Adresse et identification du bien."
                )
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = state.city,
                            onValueChange = { onEvent(HousingEditUiEvent.CityChanged(it)) },
                            label = { Text(text = "Ville") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = state.address,
                            onValueChange = { onEvent(HousingEditUiEvent.AddressChanged(it)) },
                            label = { Text(text = "Adresse") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                AppSectionHeader(
                    title = "Conditions financières",
                    supportingText = "Montants exprimés en cents."
                )
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = state.defaultRentCents.toString(),
                            onValueChange = {
                                onEvent(HousingEditUiEvent.DefaultRentChanged(it.toLongOrNull() ?: 0L))
                            },
                            label = { Text(text = "Loyer (cents)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = state.defaultChargesCents.toString(),
                            onValueChange = {
                                onEvent(HousingEditUiEvent.DefaultChargesChanged(it.toLongOrNull() ?: 0L))
                            },
                            label = { Text(text = "Charges (cents)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = state.depositCents.toString(),
                            onValueChange = {
                                onEvent(HousingEditUiEvent.DepositChanged(it.toLongOrNull() ?: 0L))
                            },
                            label = { Text(text = "Caution (cents)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                AppSectionHeader(
                    title = "Compléments",
                    supportingText = "Informations optionnelles."
                )
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = state.peb.orEmpty(),
                            onValueChange = {
                                onEvent(HousingEditUiEvent.PebChanged(it.trim().ifBlank { null }))
                            },
                            label = { Text(text = "PEB") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = state.buildingLabel.orEmpty(),
                            onValueChange = {
                                onEvent(
                                    HousingEditUiEvent.BuildingLabelChanged(it.trim().ifBlank { null })
                                )
                            },
                            label = { Text(text = "Bâtiment") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                if (state.errorMessage != null) {
                    Text(
                        text = state.errorMessage,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                PrimaryActionRow(
                    primaryLabel = "Enregistrer",
                    onPrimary = { onEvent(HousingEditUiEvent.Save) }
                )
            }
        }
    }
}
