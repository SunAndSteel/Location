package com.florent.location.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Timelapse
import androidx.compose.material.icons.outlined.TipsAndUpdates
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.florent.location.domain.model.Bail
import com.florent.location.domain.model.Housing
import com.florent.location.domain.model.Tenant

@Composable
fun TenantCard(
    tenant: Tenant,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false
) {
    val hasContact = tenant.phone != null || tenant.email != null
    val cardColors = if (isSelected) {
        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    } else {
        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    }
    Card(
        modifier = modifier.keyboardClickable(onOpen),
        colors = cardColors,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${tenant.firstName} ${tenant.lastName}",
                    style = MaterialTheme.typography.titleLarge
                )
                StatusBadge(text = if (hasContact) "Contact" else "Sans contact")
            }
            Spacer(modifier = Modifier.height(12.dp))
            val primaryContact = tenant.phone ?: tenant.email ?: "Aucun"
            val contactLabel = when {
                tenant.phone != null -> "Téléphone"
                tenant.email != null -> "Email"
                else -> "Contact"
            }
            HeroMetric(value = primaryContact, label = contactLabel)
            if (hasContact) {
                Spacer(modifier = Modifier.height(12.dp))
                DetailChipRow {
                    tenant.phone?.let {
                        MetricChip(label = it, icon = Icons.Outlined.Phone)
                    }
                    tenant.email?.let {
                        MetricChip(label = it, icon = Icons.Outlined.Email)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.TipsAndUpdates,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Ouvrir la fiche",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Supprimer le locataire"
                    )
                }
            }
        }
    }
}

@Composable
fun HousingCard(
    housing: Housing,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.keyboardClickable(onOpen),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = housing.address, style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = housing.city,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusBadge(text = "Statut inconnu")
            }
            Spacer(modifier = Modifier.height(12.dp))
            HeroMetric(
                value = formatCurrency(housing.defaultRentCents),
                label = "/ mois"
            )
            Spacer(modifier = Modifier.height(12.dp))
            DetailChipRow {
                MetricChip(
                    label = "Charges ${formatCurrency(housing.defaultChargesCents)}",
                    icon = Icons.Outlined.Payments
                )
                MetricChip(
                    label = "Caution ${formatCurrency(housing.depositCents)}",
                    icon = Icons.Outlined.Payments
                )
                housing.peb?.let {
                    MetricChip(label = "PEB $it", icon = Icons.Outlined.Home)
                }
                housing.buildingLabel?.let {
                    MetricChip(label = "Bâtiment $it", icon = Icons.Outlined.Place)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.TipsAndUpdates,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Ouvrir le logement",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Supprimer le logement"
                    )
                }
            }
        }
    }
}

@Composable
fun LeaseCard(
    bail: Bail,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isActive = bail.endDateEpochDay == null
    val statusLabel = if (isActive) "Actif" else "Terminé"
    val variant = when {
        isActive -> CardVariant.Highlighted
        else -> CardVariant.Warning
    }
    Card(
        modifier = modifier.keyboardClickable(onOpen),
        colors = variantCardColors(variant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Bail #${bail.id}",
                    style = MaterialTheme.typography.titleLarge
                )
                StatusBadge(
                    text = statusLabel,
                    containerColor = if (isActive) {
                        MaterialTheme.colorScheme.tertiaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHigh
                    }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            HeroMetric(
                value = formatCurrency(bail.rentCents),
                label = "/ mois"
            )
            Spacer(modifier = Modifier.height(12.dp))
            DetailChipRow {
                NonInteractiveChip(
                    label = "Logement #${bail.housingId}",
                    icon = Icons.Outlined.Home
                )
                NonInteractiveChip(
                    label = "Locataire #${bail.tenantId}",
                    icon = Icons.Outlined.Person
                )
                NonInteractiveChip(
                    label = "Échéance le ${bail.rentDueDayOfMonth}",
                    icon = Icons.Outlined.Timelapse
                )
                NonInteractiveChip(
                    label = "Charges ${formatCurrency(bail.chargesCents)}",
                    icon = Icons.Outlined.Payments
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            LabeledValueRow(
                label = "Début",
                value = formatEpochDay(bail.startDateEpochDay)
            )
            if (!isActive && bail.endDateEpochDay != null) {
                Spacer(modifier = Modifier.height(6.dp))
                LabeledValueRow(
                    label = "Fin",
                    value = formatEpochDay(bail.endDateEpochDay)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.TipsAndUpdates,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Ouvrir le bail",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
