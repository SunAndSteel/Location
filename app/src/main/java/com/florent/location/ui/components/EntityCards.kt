@file:OptIn(ExperimentalLayoutApi::class)

package com.florent.location.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.florent.location.domain.model.Bail
import com.florent.location.domain.model.Housing
import com.florent.location.domain.model.Tenant
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun TenantCard(
    tenant: Tenant,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false
) {
    val cardColors = if (isSelected) {
        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    } else {
        CardDefaults.cardColors()
    }
    Card(
        modifier = modifier,
        colors = cardColors,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${tenant.firstName} ${tenant.lastName}",
                    style = MaterialTheme.typography.titleMedium
                )
                AssistChip(
                    onClick = {},
                    label = { Text(text = if (tenant.phone != null || tenant.email != null) "Contact" else "Sans contact") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = null
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors()
                )
            }
            if (tenant.phone != null || tenant.email != null) {
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tenant.phone?.let {
                        AssistChip(
                            onClick = {},
                            label = { Text(text = it) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Phone,
                                    contentDescription = null
                                )
                            }
                        )
                    }
                    tenant.email?.let {
                        AssistChip(
                            onClick = {},
                            label = { Text(text = it) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Email,
                                    contentDescription = null
                                )
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onOpen) {
                    Text(text = "Voir")
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
        modifier = modifier,
        colors = CardDefaults.cardColors(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = housing.address, style = MaterialTheme.typography.titleMedium)
            Text(text = housing.city, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(
                    onClick = {},
                    label = { Text(text = "Statut: non renseigné") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Outlined.Home, contentDescription = null)
                    }
                )
                AssistChip(
                    onClick = {},
                    label = { Text(text = "Loyer: ${housing.defaultRentCents} cents") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Outlined.Payments, contentDescription = null)
                    }
                )
                AssistChip(
                    onClick = {},
                    label = { Text(text = "Charges: ${housing.defaultChargesCents} cents") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Outlined.Payments, contentDescription = null)
                    }
                )
                AssistChip(
                    onClick = {},
                    label = { Text(text = "Caution: ${housing.depositCents} cents") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Outlined.Payments, contentDescription = null)
                    }
                )
                housing.peb?.let {
                    AssistChip(
                        onClick = {},
                        label = { Text(text = "PEB: $it") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Outlined.Home, contentDescription = null)
                        }
                    )
                }
                housing.buildingLabel?.let {
                    AssistChip(
                        onClick = {},
                        label = { Text(text = "Bâtiment: $it") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Outlined.Place, contentDescription = null)
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onOpen) {
                    Text(text = "Voir")
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
    val statusLabel = if (bail.endDateEpochDay == null) "Actif" else "Terminé"
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Bail #${bail.id}", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(
                    onClick = {},
                    label = { Text(text = "Logement: ${bail.housingId}") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Outlined.Home, contentDescription = null)
                    }
                )
                AssistChip(
                    onClick = {},
                    label = { Text(text = "Locataire: ${bail.tenantId}") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Outlined.Person, contentDescription = null)
                    }
                )
                AssistChip(
                    onClick = {},
                    label = { Text(text = statusLabel) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Outlined.Schedule, contentDescription = null)
                    }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Loyer: ${bail.rentCents} cents",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Charges: ${bail.chargesCents} cents",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Début: ${formatEpochDay(bail.startDateEpochDay)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                TextButton(onClick = onOpen) {
                    Text(text = "Voir")
                }
            }
        }
    }
}

private fun formatEpochDay(epochDay: Long): String {
    return LocalDate.ofEpochDay(epochDay).format(DateTimeFormatter.ISO_LOCAL_DATE)
}
