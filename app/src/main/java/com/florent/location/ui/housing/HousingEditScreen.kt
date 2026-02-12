package com.florent.location.ui.housing

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.florent.location.domain.model.PebRating
import com.florent.location.domain.model.toDisplayLabel
import com.florent.location.ui.components.ExpressiveLoadingState
import com.florent.location.ui.components.MoneyField
import com.florent.location.ui.components.ScreenScaffold
import com.florent.location.ui.components.SectionCard
import com.florent.location.ui.components.UiTokens
import com.florent.location.ui.components.formatCurrency
import com.florent.location.ui.components.parseEuroInputToCents
import kotlinx.coroutines.delay

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
    // Navigation automatique après sauvegarde
    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            delay(300) // Petit délai pour l'animation
            onSaved()
        }
    }

    // Calcul de la validation du formulaire
    val validationState = remember(state) {
        FormValidationState(
            isStreetValid = state.street.isNotBlank(),
            isNumberValid = state.number.isNotBlank(),
            isZipCodeValid = state.zipCode.isNotBlank(),
            isCityValid = state.city.isNotBlank(),
            isRentValid = parseEuroInputToCents(state.defaultRent) != null && parseEuroInputToCents(state.defaultRent)!! > 0,
            isChargesValid = parseEuroInputToCents(state.defaultCharges) != null,
            isDepositValid = parseEuroInputToCents(state.deposit) != null
        )
    }

    val isFormValid = validationState.isFormValid
    val completionPercentage = validationState.completionPercentage

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
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                // Indicateur de progression
                FormProgressIndicator(
                    completionPercentage = completionPercentage,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = UiTokens.SpacingL)
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(UiTokens.SpacingL)
                ) {
                    // Section Informations
                    FormSection(
                        title = "Informations essentielles",
                        subtitle = "Ces champs sont obligatoires",
                        isCompleted =
                            validationState.isStreetValid &&
                                validationState.isNumberValid &&
                                validationState.isZipCodeValid &&
                                validationState.isCityValid
                    ) {
                        ValidatedTextField(
                            value = state.street,
                            onValueChange = { onEvent(HousingEditUiEvent.StreetChanged(it)) },
                            label = "Rue",
                            isValid = validationState.isStreetValid,
                            errorMessage = "La rue est obligatoire",
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(UiTokens.SpacingM),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            ValidatedTextField(
                                value = state.number,
                                onValueChange = { onEvent(HousingEditUiEvent.NumberChanged(it)) },
                                label = "Numéro",
                                isValid = validationState.isNumberValid,
                                errorMessage = "Le numéro est obligatoire",
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = state.box,
                                onValueChange = { onEvent(HousingEditUiEvent.BoxChanged(it)) },
                                label = { Text("Boîte") },
                                placeholder = { Text("Bte 2") },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(UiTokens.SpacingM),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            ValidatedTextField(
                                value = state.zipCode,
                                onValueChange = { onEvent(HousingEditUiEvent.ZipCodeChanged(it)) },
                                label = "Code postal",
                                isValid = validationState.isZipCodeValid,
                                errorMessage = "Le code postal est obligatoire",
                                modifier = Modifier.weight(1f)
                            )

                            ValidatedTextField(
                                value = state.city,
                                onValueChange = { onEvent(HousingEditUiEvent.CityChanged(it)) },
                                label = "Ville",
                                isValid = validationState.isCityValid,
                                errorMessage = "La ville est obligatoire",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        OutlinedTextField(
                            value = state.country,
                            onValueChange = { onEvent(HousingEditUiEvent.CountryChanged(it)) },
                            label = { Text("Pays") },
                            placeholder = { Text("BE") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Section Financière avec preview
                    FinancialSection(
                        state = state,
                        onEvent = onEvent,
                        validationState = validationState
                    )

                    // Section Accès et compteurs
                    FormSection(
                        title = "Accès et compteurs",
                        subtitle = "Informations pratiques (optionnel)"
                    ) {
                        OutlinedTextField(
                            value = state.mailboxLabel,
                            onValueChange = { onEvent(HousingEditUiEvent.MailboxLabelChanged(it)) },
                            label = { Text("Étiquette boîte aux lettres") },
                            placeholder = { Text("Ex: Dupont - Apt 3B") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = state.meterGas,
                            onValueChange = { onEvent(HousingEditUiEvent.MeterGasChanged(it)) },
                            label = { Text("Compteur gaz") },
                            placeholder = { Text("EAN 541...") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = state.meterElectricity,
                            onValueChange = { onEvent(HousingEditUiEvent.MeterElectricityChanged(it)) },
                            label = { Text("Compteur électricité") },
                            placeholder = { Text("EAN 541...") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = state.meterWater,
                            onValueChange = { onEvent(HousingEditUiEvent.MeterWaterChanged(it)) },
                            label = { Text("Compteur eau") },
                            placeholder = { Text("Numéro de compteur") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Section Optionnelle
                    FormSection(
                        title = "Informations complémentaires",
                        subtitle = "Champs optionnels"
                    ) {
                        OutlinedTextField(
                            value = state.pebRating.takeIf { it != PebRating.UNKNOWN }
                                ?.toDisplayLabel()
                                .orEmpty(),
                            onValueChange = { input ->
                                onEvent(
                                    HousingEditUiEvent.PebRatingChanged(
                                        parsePebRating(input)
                                    )
                                )
                            },
                            label = { Text("PEB (Performance Énergétique)") },
                            placeholder = { Text("Ex: A+, A, B, C") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = state.pebDate.orEmpty(),
                            onValueChange = { onEvent(HousingEditUiEvent.PebDateChanged(it.trim().ifBlank { null })) },
                            label = { Text("Année du PEB") },
                            placeholder = { Text("Ex: 2021") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = state.buildingLabel.orEmpty(),
                            onValueChange = { onEvent(HousingEditUiEvent.BuildingLabelChanged(it.trim().ifBlank { null })) },
                            label = { Text("Nom du bâtiment") },
                            placeholder = { Text("Ex: Résidence les Platanes") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = state.internalNote,
                            onValueChange = { onEvent(HousingEditUiEvent.InternalNoteChanged(it)) },
                            label = { Text("Note interne") },
                            placeholder = { Text("Informations supplémentaires...") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Message d'erreur expressif
                    AnimatedVisibility(
                        visible = state.errorMessage != null,
                        enter = slideInVertically() + fadeIn(),
                        exit = slideOutVertically() + fadeOut()
                    ) {
                        ErrorBanner(message = state.errorMessage ?: "")
                    }

                    // Bouton d'enregistrement intelligent
                    SmartSaveButton(
                        isEnabled = isFormValid,
                        onClick = { onEvent(HousingEditUiEvent.Save) },
                        completionPercentage = completionPercentage,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(UiTokens.SpacingXL))
                }
            }
        }
    }
}

// === COMPOSANTS PERSONNALISÉS ===

@Composable
private fun FormProgressIndicator(
    completionPercentage: Float,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Progression du formulaire",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${(completionPercentage * 100).toInt()}%",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = { completionPercentage },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}

@Composable
private fun FormSection(
    title: String,
    subtitle: String,
    isCompleted: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(UiTokens.SpacingS)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(
                visible = isCompleted,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = "Section complétée",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        SectionCard(content = content)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ValidatedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isValid: Boolean,
    errorMessage: String,
    modifier: Modifier = Modifier
) {
    val showError = value.isNotBlank() && !isValid
    val showSuccess = value.isNotBlank() && isValid

    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            isError = showError,
            trailingIcon = {
                AnimatedVisibility(
                    visible = showSuccess,
                    enter = scaleIn() + fadeIn(),
                    exit = scaleOut() + fadeOut()
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = "Valide",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                AnimatedVisibility(
                    visible = showError,
                    enter = scaleIn() + fadeIn(),
                    exit = scaleOut() + fadeOut()
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Error,
                        contentDescription = "Erreur",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        AnimatedVisibility(
            visible = showError,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

@Composable
private fun FinancialSection(
    state: HousingEditUiState,
    onEvent: (HousingEditUiEvent) -> Unit,
    validationState: FormValidationState
) {
    // Calcul du total en temps réel
    val rentCents = parseEuroInputToCents(state.defaultRent) ?: 0L
    val chargesCents = parseEuroInputToCents(state.defaultCharges) ?: 0L
    val totalCents = rentCents + chargesCents

    Column(verticalArrangement = Arrangement.spacedBy(UiTokens.SpacingS)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Conditions financières",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Montants mensuels obligatoires",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        SectionCard {
            MoneyField(
                label = "Loyer (€)",
                value = state.defaultRent,
                onValueChange = { onEvent(HousingEditUiEvent.DefaultRentChanged(it)) },
                supportingText = parseEuroInputToCents(state.defaultRent)?.let {
                    "Montant : ${formatCurrency(it)}"
                } ?: "Ex: 750,00",
            )

            MoneyField(
                label = "Charges (€)",
                value = state.defaultCharges,
                onValueChange = { onEvent(HousingEditUiEvent.DefaultChargesChanged(it)) },
                supportingText = parseEuroInputToCents(state.defaultCharges)?.let {
                    "Montant : ${formatCurrency(it)}"
                } ?: "Ex: 120,00",
            )

            MoneyField(
                label = "Caution (€)",
                value = state.deposit,
                onValueChange = { onEvent(HousingEditUiEvent.DepositChanged(it)) },
                supportingText = parseEuroInputToCents(state.deposit)?.let {
                    "Montant : ${formatCurrency(it)}"
                } ?: "Ex: 900,00",
            )
        }

        // Preview du total animé
        AnimatedVisibility(
            visible = totalCents > 0,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            TotalPreviewCard(totalCents = totalCents)
        }
    }
}

@Composable
private fun TotalPreviewCard(totalCents: Long) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(UiTokens.SpacingM),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Total mensuel",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Loyer + Charges",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }

            Text(
                text = formatCurrency(totalCents),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun ErrorBanner(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(UiTokens.SpacingM),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Error,
                contentDescription = "Erreur",
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
private fun SmartSaveButton(
    isEnabled: Boolean,
    onClick: () -> Unit,
    completionPercentage: Float,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isEnabled) 1f else 0.95f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (!isEnabled) {
            Text(
                text = "Complétez les champs obligatoires pour continuer",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = UiTokens.SpacingM)
            )
        }

        Button(
            onClick = onClick,
            enabled = isEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .scale(scale),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (completionPercentage == 1f)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.secondary
            )
        ) {
            Text(
                text = if (completionPercentage == 1f) "Enregistrer le logement" else "Complétez le formulaire",
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}

// === HELPERS ===

data class FormValidationState(
    val isStreetValid: Boolean,
    val isNumberValid: Boolean,
    val isZipCodeValid: Boolean,
    val isCityValid: Boolean,
    val isRentValid: Boolean,
    val isChargesValid: Boolean,
    val isDepositValid: Boolean
) {
    val isFormValid: Boolean
        get() = isStreetValid && isNumberValid && isZipCodeValid && isCityValid && isRentValid

    val completionPercentage: Float
        get() {
            val total = 7
            val completed = listOf(
                isStreetValid,
                isNumberValid,
                isZipCodeValid,
                isCityValid,
                isRentValid,
                isChargesValid,
                isDepositValid
            ).count { it }
            return completed.toFloat() / total.toFloat()
        }
}

private fun parsePebRating(input: String): PebRating {
    val normalized = input.trim().uppercase().replace("+", "_PLUS")
    return runCatching { PebRating.valueOf(normalized) }.getOrDefault(PebRating.UNKNOWN)
}
