package com.florent.location.ui.components

import java.text.NumberFormat
import java.math.RoundingMode
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val displayLocale: Locale = Locale.FRANCE

fun formatCurrency(cents: Long): String {
    val formatter = NumberFormat.getCurrencyInstance(displayLocale)
    return formatter.format(cents / 100.0)
}

fun formatEuroInput(cents: Long): String {
    val formatter = NumberFormat.getNumberInstance(displayLocale).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
    return formatter.format(cents / 100.0)
}

fun parseEuroInputToCents(value: String): Long? {
    val trimmed = value.trim()
    if (trimmed.isBlank()) return null
    val normalized = trimmed
        .replace("\u00A0", "")
        .replace(" ", "")
        .replace(",", ".")
    return runCatching {
        normalized
            .toBigDecimal()
            .movePointRight(2)
            .setScale(0, RoundingMode.HALF_UP)
            .longValueExact()
    }.getOrNull()
}

fun formatEpochDay(epochDay: Long): String {
    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", displayLocale)
    return LocalDate.ofEpochDay(epochDay).format(formatter)
}
