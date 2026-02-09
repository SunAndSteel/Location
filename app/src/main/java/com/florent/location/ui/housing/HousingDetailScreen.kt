package com.florent.location.ui.housing

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.florent.location.domain.model.Housing
import com.florent.location.domain.model.HousingSituation
import com.florent.location.domain.model.PebRating
import com.florent.location.domain.model.toDisplayLabel
import com.florent.location.ui.components.*
import kotlinx.coroutines.delay

@Composable
fun HousingDetailScreen(
    viewModel: HousingDetailViewModel,
    onEdit: () -> Unit,
    onCreateLease: () -> Unit,
    onDeleted: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showActionsSheet by remember { mutableStateOf(false) }

    HousingDetailContent(
        state = state,
        onEdit = onEdit,
        onCreateLease = onCreateLease,
        onDeleteClick = { showDeleteDialog = true },
        onShowActions = { showActionsSheet = true },
        modifier = modifier
    )

    // Modal de confirmation de suppression
    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            housingAddress = state.housing?.address?.fullString() ?: "",
            onConfirm = {
                state.housing?.let { housing ->
                    viewModel.onEvent(HousingDetailUiEvent.DeleteHousing(housing.id))
                }
                showDeleteDialog = false
                // Navigation après suppression
                onDeleted()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    // Bottom sheet d'actions (mobile)
    if (showActionsSheet) {
        ActionsBottomSheet(
            onEdit = {
                showActionsSheet = false
                onEdit()
            },
            onCreateLease = {
                showActionsSheet = false
                onCreateLease()
            },
            onDelete = {
                showActionsSheet = false
                showDeleteDialog = true
            },
            onDismiss = { showActionsSheet = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HousingDetailContent(
    state: HousingDetailUiState,
    onEdit: () -> Unit,
    onCreateLease: () -> Unit,
    onDeleteClick: () -> Unit,
    onShowActions: () -> Unit,
    modifier: Modifier = Modifier
) {
    ScreenScaffold(
        title = "Détail du logement",
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
                        val scrollState = rememberScrollState()
                        if (sizeClass == WindowWidthSize.Expanded) {
                            // Layout desktop/tablette
                            ExpandedLayout(
                                housing = housing,
                                situation = situation,
                                onEdit = onEdit,
                                onCreateLease = onCreateLease,
                                onDeleteClick = onDeleteClick,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(scrollState)
                            )
                        } else {
                            // Layout mobile
                            CompactLayout(
                                housing = housing,
                                situation = situation,
                                onShowActions = onShowActions,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(scrollState)
                            )
                        }
                    }
                }
            }
        }
    }
}

// === LAYOUTS ===

@Composable
private fun ExpandedLayout(
    housing: Housing,
    situation: HousingSituation,
    onEdit: () -> Unit,
    onCreateLease: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(UiTokens.SpacingXL)
    ) {
        // Contenu principal
        Column(
            modifier = Modifier.weight(0.6f),
            verticalArrangement = Arrangement.spacedBy(UiTokens.SpacingL)
        ) {
            EnhancedHeroSection(housing = housing, situation = situation)
            HousingInfoSection(housing = housing)
            HousingAccessSection(housing = housing)
            HousingFinancialSection(housing = housing)
        }

        // Panel d'actions
        Column(
            modifier = Modifier.weight(0.4f),
            verticalArrangement = Arrangement.spacedBy(UiTokens.SpacingL)
        ) {
            ActionsPanel(
                onEdit = onEdit,
                onCreateLease = onCreateLease,
                onDeleteClick = onDeleteClick
            )
        }
    }
}

@Composable
private fun CompactLayout(
    housing: Housing,
    situation: HousingSituation,
    onShowActions: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(UiTokens.SpacingL)
    ) {
        EnhancedHeroSection(housing = housing, situation = situation)

        // Bouton d'actions flottant pour mobile
        Button(
            onClick = onShowActions,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Icon(
                imageVector = Icons.Outlined.MoreHoriz,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Actions")
        }

        HousingInfoSection(housing = housing)
        HousingAccessSection(housing = housing)
        HousingFinancialSection(housing = housing)
    }
}

// === SECTIONS AMÉLIORÉES ===

@Composable
private fun EnhancedHeroSection(
    housing: Housing,
    situation: HousingSituation,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(UiTokens.SpacingL),
            verticalArrangement = Arrangement.spacedBy(UiTokens.SpacingM)
        ) {
            // Badge de statut animé
            SituationBadge(situation = situation)

            // Adresse avec typographie expressive
            Text(
                text = housing.address.fullString(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Text(
                text = housing.address.city,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )

            Divider(
                modifier = Modifier.padding(vertical = UiTokens.SpacingS),
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
            )

            // Prix principal avec hiérarchie forte
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "Loyer mensuel",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = formatCurrency(housing.rentCents),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "+ ${formatCurrency(housing.chargesCents)}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "charges",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }

            // Total
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(UiTokens.SpacingM),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total mensuel",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = formatCurrency(housing.rentCents + housing.chargesCents),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Caution
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Caution",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Text(
                    text = formatCurrency(housing.depositCents),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun SituationBadge(situation: HousingSituation) {
    val (label, color, icon) = when (situation) {
        HousingSituation.LIBRE -> Triple(
            "Disponible",
            MaterialTheme.colorScheme.tertiary,
            Icons.Outlined.CheckCircle
        )
        HousingSituation.OCCUPE -> Triple(
            "Occupé",
            MaterialTheme.colorScheme.error,
            Icons.Outlined.Home
        )
        HousingSituation.DRAFT -> Triple(
            "Bail se termine",
            MaterialTheme.colorScheme.secondary,
            Icons.Outlined.Schedule
        )
    }

    // Animation pulse pour statut occupé
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Surface(
        shape = MaterialTheme.shapes.small,
        color = color.copy(alpha = 0.2f),
        modifier = Modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )

            if(situation == HousingSituation.OCCUPE) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = alpha))
                )
            }

        }
    }
}

@Composable
private fun ActionsPanel(
    onEdit: () -> Unit,
    onCreateLease: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(UiTokens.SpacingL)) {
        AppSectionHeader(title = "Actions rapides")

        // Actions principales
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        ) {
            Column(
                modifier = Modifier.padding(UiTokens.SpacingM),
                verticalArrangement = Arrangement.spacedBy(UiTokens.SpacingS)
            ) {
                ActionButton(
                    text = "Modifier le logement",
                    icon = Icons.Outlined.Edit,
                    onClick = onEdit,
                    isPrimary = true
                )

                ActionButton(
                    text = "Créer un bail",
                    icon = Icons.Outlined.Add,
                    onClick = onCreateLease
                )
            }
        }

        AppSectionHeader(
            title = "Zone dangereuse",
            supportingText = "Actions irréversibles"
        )

        // Zone de suppression
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(UiTokens.SpacingM),
                verticalArrangement = Arrangement.spacedBy(UiTokens.SpacingS)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.ReportProblem,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Supprimer le logement",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }

                Text(
                    text = "Cette action supprimera définitivement le logement et tous les baux associés.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                )

                OutlinedButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.error)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Supprimer")
                }
            }
        }
    }
}

@Composable
private fun ActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    isPrimary: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = if (isPrimary) {
            ButtonDefaults.buttonColors()
        } else {
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text)
    }
}

// Sections info (identiques mais avec SectionCard au lieu de Card simple)

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
            LabeledValueRow(label = "Adresse", value = housing.address.fullString())
            LabeledValueRow(label = "Ville", value = housing.address.city)
            LabeledValueRow(
                label = "PEB",
                value = housing.pebRating.takeIf { it != PebRating.UNKNOWN }?.toDisplayLabel()
                    ?: "Non renseigné"
            )
            LabeledValueRow(label = "Année PEB", value = housing.pebDate ?: "Non renseigné")
            LabeledValueRow(label = "Bâtiment", value = housing.buildingLabel ?: "Non renseigné")
            LabeledValueRow(label = "Note interne", value = housing.internalNote ?: "Non renseigné")
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
            LabeledValueRow(label = "Loyer", value = formatCurrency(housing.rentCents))
            LabeledValueRow(label = "Charges", value = formatCurrency(housing.chargesCents))
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
            LabeledValueRow(label = "Compteur gaz", value = housing.meterGasId ?: "Non renseigné")
            LabeledValueRow(label = "Compteur électricité", value = housing.meterElectricityId ?: "Non renseigné")
            LabeledValueRow(label = "Compteur eau", value = housing.meterWaterId ?: "Non renseigné")
        }
    }
}

// === DIALOGS & BOTTOM SHEETS ===

@Composable
private fun DeleteConfirmationDialog(
    housingAddress: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    var confirmText by remember { mutableStateOf("") }
    val isConfirmEnabled = confirmText.equals("SUPPRIMER", ignoreCase = true)

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Outlined.ReportProblem,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = "Supprimer ce logement ?",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Vous êtes sur le point de supprimer définitivement :",
                    style = MaterialTheme.typography.bodyMedium
                )

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = housingAddress,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Text(
                    text = "⚠️ Cette action supprimera également tous les baux associés. Cette opération est irréversible.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Pour confirmer, tapez SUPPRIMER :",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )

                OutlinedTextField(
                    value = confirmText,
                    onValueChange = { confirmText = it },
                    placeholder = { Text("SUPPRIMER") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = isConfirmEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Supprimer définitivement")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActionsBottomSheet(
    onEdit: () -> Unit,
    onCreateLease: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(UiTokens.SpacingL)
                .padding(bottom = UiTokens.SpacingXL),
            verticalArrangement = Arrangement.spacedBy(UiTokens.SpacingM)
        ) {
            Text(
                text = "Actions",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Divider()

            BottomSheetAction(
                text = "Modifier le logement",
                icon = Icons.Outlined.Edit,
                onClick = onEdit
            )

            BottomSheetAction(
                text = "Créer un bail",
                icon = Icons.Outlined.Add,
                onClick = onCreateLease
            )

            Divider()

            BottomSheetAction(
                text = "Supprimer le logement",
                icon = Icons.Outlined.Delete,
                onClick = onDelete,
                isDestructive = true
            )
        }
    }
}

@Composable
private fun BottomSheetAction(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = if (isDestructive)
            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        else
            Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(UiTokens.SpacingM),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
