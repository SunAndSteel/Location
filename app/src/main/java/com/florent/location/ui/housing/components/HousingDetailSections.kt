package com.florent.location.ui.housing.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.HomeWork
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.ReportProblem
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.florent.location.domain.model.Housing
import com.florent.location.domain.model.HousingSituation
import com.florent.location.domain.model.PebRating
import com.florent.location.domain.model.toDisplayLabel
import com.florent.location.ui.components.ExpressiveEmptyState
import com.florent.location.ui.components.ExpressiveErrorState
import com.florent.location.ui.components.ExpressiveLoadingState
import com.florent.location.ui.components.LabeledValueRow
import com.florent.location.ui.components.ScreenScaffold
import com.florent.location.ui.components.SectionCard
import com.florent.location.ui.components.SectionHeader
import com.florent.location.ui.components.UiTokens
import com.florent.location.ui.components.WindowWidthSize
import com.florent.location.ui.components.formatCurrency
import com.florent.location.ui.components.windowWidthSize
import com.florent.location.ui.housing.HousingDetailUiState

@Composable
internal fun HousingDetailContent(
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
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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

@Composable
private fun ExpandedLayout(
    housing: Housing,
    situation: HousingSituation,
    onEdit: () -> Unit,
    onCreateLease: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(UiTokens.SpacingXL)) {
        Column(
            modifier = Modifier.weight(0.6f),
            verticalArrangement = Arrangement.spacedBy(UiTokens.SpacingL)
        ) {
            EnhancedHeroSection(housing = housing, situation = situation)
            HousingInfoSection(housing = housing)
            HousingAccessSection(housing = housing)
            HousingFinancialSection(housing = housing)
        }

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
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(UiTokens.SpacingL)) {
        EnhancedHeroSection(housing = housing, situation = situation)

        Button(
            onClick = onShowActions,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Icon(imageVector = Icons.Outlined.MoreHoriz, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Actions")
        }

        HousingInfoSection(housing = housing)
        HousingAccessSection(housing = housing)
        HousingFinancialSection(housing = housing)
    }
}

@Composable
private fun EnhancedHeroSection(housing: Housing, situation: HousingSituation, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp,
            focusedElevation = 1.dp,
            hoveredElevation = 1.dp
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(UiTokens.SpacingL),
            verticalArrangement = Arrangement.spacedBy(UiTokens.SpacingM)
        ) {
            SituationBadge(situation = situation)
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

            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                border = CardDefaults.outlinedCardBorder(enabled = true),
                shape = MaterialTheme.shapes.medium
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(UiTokens.SpacingM),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total mensuel", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                    Text(
                        text = formatCurrency(housing.rentCents + housing.chargesCents),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
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
        HousingSituation.LIBRE -> Triple("Disponible", MaterialTheme.colorScheme.tertiary, Icons.Outlined.CheckCircle)
        HousingSituation.OCCUPE -> Triple("Occupé", MaterialTheme.colorScheme.error, Icons.Outlined.Home)
        HousingSituation.DRAFT -> Triple("Bail se termine", MaterialTheme.colorScheme.secondary, Icons.Outlined.Schedule)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(1000), repeatMode = RepeatMode.Reverse),
        label = "alpha"
    )

    Surface(shape = MaterialTheme.shapes.small, color = color.copy(alpha = 0.2f)) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            Text(text = label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = color)

            if (situation == HousingSituation.OCCUPE) {
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
private fun ActionsPanel(onEdit: () -> Unit, onCreateLease: () -> Unit, onDeleteClick: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(UiTokens.SpacingL)) {
        SectionHeader(title = "Actions rapides")
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 0.dp,
                focusedElevation = 1.dp,
                hoveredElevation = 1.dp
            ),
            shape = MaterialTheme.shapes.medium
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
                ActionButton(text = "Créer un bail", icon = Icons.Outlined.Add, onClick = onCreateLease)
            }
        }

        SectionHeader(title = "Zone dangereuse", supportingText = "Actions irréversibles")
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 0.dp,
                focusedElevation = 1.dp,
                hoveredElevation = 1.dp
            ),
            shape = MaterialTheme.shapes.medium
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
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.error)
                    )
                ) {
                    Icon(imageVector = Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Supprimer")
                }
            }
        }
    }
}

@Composable
private fun ActionButton(text: String, icon: ImageVector, onClick: () -> Unit, isPrimary: Boolean = false) {
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
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text)
    }
}

@Composable
private fun HousingInfoSection(housing: Housing, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(UiTokens.SpacingS)) {
        SectionHeader(title = "Informations", supportingText = "Détails complémentaires sur le bien.")
        SectionCard {
            LabeledValueRow(label = "Adresse", value = housing.address.fullString())
            LabeledValueRow(label = "Ville", value = housing.address.city)
            LabeledValueRow(
                label = "PEB",
                value = housing.pebRating.takeIf { it != PebRating.UNKNOWN }?.toDisplayLabel() ?: "Non renseigné"
            )
            LabeledValueRow(label = "Année PEB", value = housing.pebDate ?: "Non renseigné")
            LabeledValueRow(label = "Bâtiment", value = housing.buildingLabel ?: "Non renseigné")
            LabeledValueRow(label = "Note interne", value = housing.internalNote ?: "Non renseigné")
        }
    }
}

@Composable
private fun HousingFinancialSection(housing: Housing, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(UiTokens.SpacingS)) {
        SectionHeader(
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
private fun HousingAccessSection(housing: Housing, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(UiTokens.SpacingS)) {
        SectionHeader(
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

@Composable
internal fun HousingDeleteConfirmationDialog(
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
        title = { Text(text = "Supprimer ce logement ?", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Vous êtes sur le point de supprimer définitivement :",
                    style = MaterialTheme.typography.bodyMedium
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 0.dp,
                        focusedElevation = 1.dp,
                        hoveredElevation = 1.dp
                    ),
                    shape = MaterialTheme.shapes.medium
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
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
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
internal fun HousingActionsBottomSheet(
    onEdit: () -> Unit,
    onCreateLease: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(UiTokens.SpacingL)
                .padding(bottom = UiTokens.SpacingXL),
            verticalArrangement = Arrangement.spacedBy(UiTokens.SpacingM)
        ) {
            Text(text = "Actions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Divider()
            BottomSheetAction(text = "Modifier le logement", icon = Icons.Outlined.Edit, onClick = onEdit)
            BottomSheetAction(text = "Créer un bail", icon = Icons.Outlined.Add, onClick = onCreateLease)
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
    icon: ImageVector,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = if (isDestructive) {
            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        } else {
            Color.Transparent
        }
    ) {
        Row(modifier = Modifier.padding(UiTokens.SpacingM), verticalAlignment = Alignment.CenterVertically) {
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
