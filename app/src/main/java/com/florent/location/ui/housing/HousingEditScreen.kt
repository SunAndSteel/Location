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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.florent.location.ui.components.AppSectionHeader
import com.florent.location.ui.components.ExpressiveLoadingState
import com.florent.location.ui.components.MoneyField
import com.florent.location.ui.components.PrimaryActionRow
import com.florent.location.ui.components.ScreenScaffold
import com.florent.location.ui.components.SectionCard
import com.florent.location.ui.components.UiTokens
import com.florent.location.ui.components.formatCurrency

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

    ScreenScaffold(
        title = if (state.housingId == null) "Nouveau logement" else "Modifier le logement",
        contentMaxWidth = UiTokens.ContentMaxWidthMedium,
        modifier = modifier
    ) {
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
        } else {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(UiTokens.SpacingL)
            ) {
                AppSectionHeader(
                    title = "Informations",
                    supportingText = "Adresse et identification du bien."
                )
            SectionCard {
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

            AppSectionHeader(
                title = "Conditions financières",
                supportingText = "Saisissez un montant en euros, ex: 750,00."
            )
            SectionCard {
                MoneyField(
                    label = "Loyer (€)",
                    value = state.defaultRentCents.toString(),
                    onValueChange = {
                        onEvent(HousingEditUiEvent.DefaultRentChanged(it.toLongOrNull() ?: 0L))
                    },
                    supportingText = "Actuel : ${formatCurrency(state.defaultRentCents)}"
                )
                MoneyField(
                    label = "Charges (€)",
                    value = state.defaultChargesCents.toString(),
                    onValueChange = {
                        onEvent(HousingEditUiEvent.DefaultChargesChanged(it.toLongOrNull() ?: 0L))
                    },
                    supportingText = "Actuel : ${formatCurrency(state.defaultChargesCents)}"
                )
                MoneyField(
                    label = "Caution (€)",
                    value = state.depositCents.toString(),
                    onValueChange = {
                        onEvent(HousingEditUiEvent.DepositChanged(it.toLongOrNull() ?: 0L))
                    },
                    supportingText = "Actuel : ${formatCurrency(state.depositCents)}"
                )
            }

            AppSectionHeader(
                title = "Accès et compteurs",
                supportingText = "Informations liées à la boîte aux lettres et aux compteurs."
            )
            SectionCard {
                OutlinedTextField(
                    value = state.mailboxLabel,
                    onValueChange = {
                        onEvent(HousingEditUiEvent.MailboxLabelChanged(it))
                    },
                    label = { Text(text = "Étiquette boîte aux lettres") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = state.meterGas,
                    onValueChange = {
                        onEvent(HousingEditUiEvent.MeterGasChanged(it))
                    },
                    label = { Text(text = "Compteur gaz") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = state.meterElectricity,
                    onValueChange = {
                        onEvent(HousingEditUiEvent.MeterElectricityChanged(it))
                    },
                    label = { Text(text = "Compteur électricité") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = state.meterWater,
                    onValueChange = {
                        onEvent(HousingEditUiEvent.MeterWaterChanged(it))
                    },
                    label = { Text(text = "Compteur eau") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            AppSectionHeader(
                title = "Champs optionnels",
                supportingText = "Informations complémentaires."
            )
            SectionCard {
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
