@file:OptIn(ExperimentalMaterial3Api::class)

package com.florent.location.ui.tenant.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.HomeWork
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.PauseCircle
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ReportProblem
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledCard
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.florent.location.domain.model.Tenant
import com.florent.location.domain.model.TenantSituation
import com.florent.location.domain.model.TenantStatus
import com.florent.location.ui.components.ExpressiveEmptyState
import com.florent.location.ui.components.ExpressiveErrorState
import com.florent.location.ui.components.ExpressiveLoadingState
import com.florent.location.ui.components.LabeledValueRow
import com.florent.location.ui.components.ScreenScaffold
import com.florent.location.ui.components.SectionCard
import com.florent.location.ui.components.SectionHeader
import com.florent.location.ui.components.StatusBadge
import com.florent.location.ui.components.UiTokens
import com.florent.location.ui.components.WindowWidthSize
import com.florent.location.ui.components.tenantSituationLabel
import com.florent.location.ui.components.tenantStatusLabel
import com.florent.location.ui.components.windowWidthSize
import com.florent.location.ui.tenant.TenantDetailUiEvent
import com.florent.location.ui.tenant.TenantDetailUiState

@Composable
internal fun TenantDetailContent(
    state: TenantDetailUiState,
    onEvent: (TenantDetailUiEvent) -> Unit,
    onEdit: () -> Unit,
    onCreateLease: () -> Unit,
    onShowActions: () -> Unit,
    modifier: Modifier = Modifier
) {
    ScreenScaffold(
        title = "Locataire",
        contentMaxWidth = UiTokens.ContentMaxWidthExpanded,
        modifier = modifier
    ) {
        when {
            state.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    ExpressiveLoadingState(
                        title = "Chargement du locataire",
                        message = "Nous préparons la fiche du contact."
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
                        title = "Impossible de charger le locataire",
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
                        title = "Locataire introuvable",
                        message = "Ce contact n'est plus disponible.",
                        icon = Icons.Outlined.Person
                    )
                }
            }

            state.tenant != null -> {
                val tenant = state.tenant
                BoxWithConstraints {
                    val sizeClass = windowWidthSize(maxWidth)
                    val scrollState = rememberScrollState()
                    if (sizeClass == WindowWidthSize.Expanded) {
                        Row(
                            modifier = Modifier.fillMaxWidth().verticalScroll(scrollState),
                            horizontalArrangement = Arrangement.spacedBy(UiTokens.SpacingXL)
                        ) {
                            Column(
                                modifier = Modifier.weight(0.6f),
                                verticalArrangement = Arrangement.spacedBy(UiTokens.SpacingL)
                            ) {
                                TenantHeroSection(tenant = tenant, situation = state.situation)
                                TenantContactSection(tenant = tenant)
                                TenantSituationSection(situation = state.situation)
                            }
                            Column(
                                modifier = Modifier.weight(0.4f),
                                verticalArrangement = Arrangement.spacedBy(UiTokens.SpacingL)
                            ) {
                                TenantActionsPanel(
                                    onEdit = {
                                        onEvent(TenantDetailUiEvent.Edit)
                                        onEdit()
                                    },
                                    onCreateLease = onCreateLease,
                                    onDelete = { onEvent(TenantDetailUiEvent.Delete) }
                                )
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth().verticalScroll(scrollState),
                            verticalArrangement = Arrangement.spacedBy(UiTokens.SpacingL)
                        ) {
                            TenantHeroSection(tenant = tenant, situation = state.situation)
                            Button(
                                onClick = onShowActions,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                Icon(imageVector = Icons.Outlined.MoreHoriz, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Actions")
                            }
                            TenantContactSection(tenant = tenant)
                            TenantSituationSection(situation = state.situation)
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun TenantActionsBottomSheet(
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
            BottomSheetAction(text = "Modifier le locataire", icon = Icons.Outlined.Edit, onClick = onEdit)
            BottomSheetAction(text = "Créer un bail", icon = Icons.Outlined.Add, onClick = onCreateLease)
            Divider()
            BottomSheetAction(
                text = "Supprimer le locataire",
                icon = Icons.Outlined.Delete,
                onClick = onDelete,
                isDestructive = true
            )
        }
    }
}

@Composable
private fun TenantHeroSection(tenant: Tenant, situation: TenantSituation?, modifier: Modifier = Modifier) {
    FilledCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.filledCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.filledCardElevation(
            defaultElevation = 0.dp,
            focusedElevation = 1.dp,
            hoveredElevation = 1.dp
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(UiTokens.SpacingL), verticalArrangement = Arrangement.spacedBy(UiTokens.SpacingM)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(UiTokens.SpacingS),
                verticalAlignment = Alignment.CenterVertically
            ) {
                situation?.let {
                    StatusBadge(
                        text = tenantStatusLabel(it.status),
                        icon = tenantStatusIcon(it.status),
                        color = tenantStatusColor(it.status)
                    )
                }
                situation?.let {
                    StatusBadge(
                        text = if (it.hasActiveLease) "Bail actif" else "Sans bail",
                        icon = Icons.Outlined.HomeWork,
                        color = if (it.hasActiveLease) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline
                    )
                }
            }

            Text(
                text = "${tenant.firstName} ${tenant.lastName}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Text(
                text = tenantSituationLabel(situation ?: TenantSituation(status = tenant.status, hasActiveLease = false)),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(
                        text = "Téléphone",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = tenant.phone ?: "Non renseigné",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Email",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = tenant.email ?: "Non renseigné",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun TenantContactSection(tenant: Tenant, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(UiTokens.SpacingS)) {
        SectionHeader(title = "Contact", supportingText = "Coordonnées principales.")
        SectionCard {
            LabeledValueRow(label = "Téléphone", value = tenant.phone ?: "Non renseigné")
            LabeledValueRow(label = "Email", value = tenant.email ?: "Non renseigné")
        }
    }
}

@Composable
private fun TenantSituationSection(situation: TenantSituation?, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(UiTokens.SpacingS)) {
        SectionHeader(title = "Situation", supportingText = "Statut et bail en cours.")
        SectionCard {
            LabeledValueRow(
                label = "Statut",
                value = situation?.let { tenantStatusLabel(it.status) } ?: "Non renseigné"
            )
            LabeledValueRow(
                label = "Bail",
                value = when {
                    situation == null -> "Non renseigné"
                    situation.hasActiveLease -> "Actif"
                    else -> "Sans bail"
                }
            )
        }
    }
}

@Composable
private fun TenantActionsPanel(
    onEdit: () -> Unit,
    onCreateLease: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(UiTokens.SpacingL)) {
        SectionHeader(title = "Actions rapides")
        FilledCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.filledCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            elevation = CardDefaults.filledCardElevation(
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
                    text = "Modifier le locataire",
                    icon = Icons.Outlined.Edit,
                    onClick = onEdit,
                    isPrimary = true
                )
                ActionButton(text = "Créer un bail", icon = Icons.Outlined.Add, onClick = onCreateLease)
            }
        }

        SectionHeader(title = "Zone dangereuse", supportingText = "Actions irréversibles")

        FilledCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.filledCardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
            elevation = CardDefaults.filledCardElevation(
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
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Supprimer le locataire",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                Text(
                    text = "Cette action est définitive et supprimera le locataire.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                )
                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
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
        color = if (isDestructive) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f) else Color.Transparent
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

@Composable
private fun tenantStatusColor(status: TenantStatus): Color =
    when (status) {
        TenantStatus.ACTIVE -> MaterialTheme.colorScheme.tertiary
        TenantStatus.LOOKING -> MaterialTheme.colorScheme.secondary
        TenantStatus.INACTIVE -> MaterialTheme.colorScheme.error
    }

private fun tenantStatusIcon(status: TenantStatus) =
    when (status) {
        TenantStatus.ACTIVE -> Icons.Outlined.CheckCircle
        TenantStatus.LOOKING -> Icons.Outlined.Search
        TenantStatus.INACTIVE -> Icons.Outlined.PauseCircle
    }
