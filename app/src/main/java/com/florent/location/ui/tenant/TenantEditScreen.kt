@file:OptIn(ExperimentalMaterial3Api::class)

package com.florent.location.ui.tenant

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.florent.location.ui.components.ExpressiveLoadingState
import com.florent.location.ui.components.PrimaryActionRow
import com.florent.location.ui.components.ScreenScaffold
import com.florent.location.ui.components.SectionCard
import com.florent.location.ui.components.tenantStatusLabel
import com.florent.location.ui.components.UiTokens
import com.florent.location.ui.components.experimental.ExposedDropdownMenu
import com.florent.location.domain.model.TenantStatus

@Composable
fun TenantEditScreen(
    viewModel: TenantEditViewModel,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    TenantEditContent(
        state = state,
        onEvent = viewModel::onEvent,
        onSaved = onSaved,
        modifier = modifier
    )
}

@Composable
private fun TenantEditContent(
    state: TenantEditUiState,
    onEvent: (TenantEditUiEvent) -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            onSaved()
        }
    }

    ScreenScaffold(
        title = if (state.tenantId == null) "Nouveau locataire" else "Modifier le locataire",
        contentMaxWidth = UiTokens.ContentMaxWidthMedium,
        modifier = modifier
    ) {
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                ExpressiveLoadingState(
                    title = "Chargement du locataire",
                    message = "Nous préparons le formulaire."
                )
            }
        } else {
            val completionState = remember(state) {
                TenantFormProgress(
                    isFirstNameFilled = state.firstName.isNotBlank(),
                    isLastNameFilled = state.lastName.isNotBlank(),
                    isPhoneFilled = state.phone.isNotBlank(),
                    isEmailFilled = state.email.isNotBlank()
                )
            }
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(UiTokens.SpacingL)
            ) {
                FormProgressIndicator(
                    completionPercentage = completionState.completionPercentage,
                    modifier = Modifier.fillMaxWidth()
                )

                FormSection(
                    title = "Identité",
                    subtitle = "Informations principales du locataire.",
                    isCompleted = completionState.isIdentityComplete
                ) {
                    OutlinedTextField(
                        value = state.firstName,
                        onValueChange = {
                            onEvent(TenantEditUiEvent.FieldChanged(TenantField.FirstName, it))
                        },
                        label = { Text(text = "Prénom") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = state.lastName,
                        onValueChange = {
                            onEvent(TenantEditUiEvent.FieldChanged(TenantField.LastName, it))
                        },
                        label = { Text(text = "Nom") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                FormSection(
                    title = "Statut",
                    subtitle = "Situation du locataire.",
                    isCompleted = true
                ) {
                    ExposedDropdownMenuBox(
                        expanded = state.statusDropdownExpanded,
                        onExpandedChange = { onEvent(TenantEditUiEvent.StatusDropdownExpanded(it)) }
                    ) {
                        OutlinedTextField(
                            value = tenantStatusLabel(state.status),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(text = "Statut") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(
                                    expanded = state.statusDropdownExpanded
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = state.statusDropdownExpanded,
                            onDismissRequest = {
                                onEvent(TenantEditUiEvent.StatusDropdownExpanded(false))
                            }
                        ) {
                            TenantStatus.entries.forEach { status ->
                                DropdownMenuItem(
                                    text = { Text(text = tenantStatusLabel(status)) },
                                    onClick = {
                                        onEvent(TenantEditUiEvent.StatusChanged(status))
                                        onEvent(TenantEditUiEvent.StatusDropdownExpanded(false))
                                    }
                                )
                            }
                        }
                    }
                }

                FormSection(
                    title = "Contact",
                    subtitle = "Coordonnées du locataire.",
                    isCompleted = completionState.isContactComplete
                ) {
                    OutlinedTextField(
                        value = state.phone,
                        onValueChange = {
                            onEvent(TenantEditUiEvent.FieldChanged(TenantField.Phone, it))
                        },
                        label = { Text(text = "Téléphone") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = state.email,
                        onValueChange = {
                            onEvent(TenantEditUiEvent.FieldChanged(TenantField.Email, it))
                        },
                        label = { Text(text = "Email") },
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
                    onPrimary = { onEvent(TenantEditUiEvent.SaveClicked) }
                )
            }
        }
    }
}

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

        LinearProgressIndicator(
            progress = { completionPercentage },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
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
                androidx.compose.material3.Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = "Section complétée",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        SectionCard(content = content)
    }
}

private data class TenantFormProgress(
    val isFirstNameFilled: Boolean,
    val isLastNameFilled: Boolean,
    val isPhoneFilled: Boolean,
    val isEmailFilled: Boolean
) {
    val isIdentityComplete: Boolean
        get() = isFirstNameFilled && isLastNameFilled

    val isContactComplete: Boolean
        get() = isPhoneFilled || isEmailFilled

    val completionPercentage: Float
        get() {
            val total = 4
            val completed = listOf(
                isFirstNameFilled,
                isLastNameFilled,
                isPhoneFilled,
                isEmailFilled
            ).count { it }
            return completed.toFloat() / total.toFloat()
        }
}
