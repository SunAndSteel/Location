package com.florent.location.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.TextStyle
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateFieldWithPicker(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    supportingText: String? = null
) {
    var isDialogOpen by remember { mutableStateOf(false) }
    val initialDate = remember(value) { parseIsoDate(value) }
    val initialMillis = remember(initialDate) { initialDate?.let { toEpochMillis(it) } }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialMillis ?: System.currentTimeMillis()
    )
    val interactionSource = remember { MutableInteractionSource() }

    LaunchedEffect(isDialogOpen, initialMillis) {
        if (isDialogOpen) {
            datePickerState.selectedDateMillis = initialMillis ?: System.currentTimeMillis()
        }
    }

    OutlinedTextField(
        value = initialDate?.let { formatEpochDay(it.toEpochDay()) } ?: "",
        onValueChange = {},
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                isDialogOpen = true
            }
            .onPreviewKeyEvent { event ->
                val isOpenKey = event.key == Key.Enter ||
                    event.key == Key.NumPadEnter ||
                    event.key == Key.Spacebar ||
                    event.key == Key.DirectionCenter
                if (isOpenKey && event.type == KeyEventType.KeyUp) {
                    isDialogOpen = true
                    true
                } else {
                    false
                }
            },
        label = { Text(text = label) },
        readOnly = true,
        trailingIcon = {
            IconButton(onClick = { isDialogOpen = true }) {
                Icon(imageVector = Icons.Outlined.CalendarMonth, contentDescription = null)
            }
        },
        supportingText = supportingText?.let { { Text(text = it) } }
    )

    if (isDialogOpen) {
        DatePickerDialog(
            onDismissRequest = { isDialogOpen = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val millis = datePickerState.selectedDateMillis
                        if (millis != null) {
                            val newDate = java.time.Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                            onValueChange(newDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
                        }
                        isDialogOpen = false
                    }
                ) {
                    Text(text = "Valider")
                }
            },
            dismissButton = {
                TextButton(onClick = { isDialogOpen = false }) {
                    Text(text = "Annuler")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun MoneyField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(text = label) },
        supportingText = supportingText?.let { { Text(text = it) } }
    )
}

@Composable
fun MoneyText(
    cents: Long,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Text(
        text = formatCurrency(cents),
        modifier = modifier,
        style = style,
        color = color
    )
}

@Composable
fun DateText(
    epochDay: Long,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Text(
        text = formatEpochDay(epochDay),
        modifier = modifier,
        style = style,
        color = color
    )
}

private fun parseIsoDate(value: String): LocalDate? =
    runCatching { LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE) }.getOrNull()

private fun toEpochMillis(date: LocalDate): Long =
    date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
