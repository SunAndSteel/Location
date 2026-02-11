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

    val compact = trimmed
        .replace("€", "")
        .replace("\u00A0", "")
        .replace("\u202F", "")
        .replace(" ", "")

    if (compact.isBlank()) return null

    val sign = compact.takeIf { it.startsWith("-") || it.startsWith("+") }?.firstOrNull()
    val unsigned = if (sign != null) compact.drop(1) else compact
    if (unsigned.isBlank()) return null

    val lastComma = unsigned.lastIndexOf(',')
    val lastDot = unsigned.lastIndexOf('.')
    val decimalSeparator = when {
        lastComma >= 0 && lastDot >= 0 -> if (lastComma > lastDot) ',' else '.'
        lastComma >= 0 -> if (unsigned.length - lastComma - 1 in 1..2) ',' else null
        lastDot >= 0 -> if (unsigned.length - lastDot - 1 in 1..2) '.' else null
        else -> null
    }

    val normalizedNumber = buildString {
        unsigned.forEachIndexed { index, char ->
            when {
                char.isDigit() -> append(char)
                decimalSeparator != null && char == decimalSeparator && index == unsigned.lastIndexOf(decimalSeparator) -> append('.')
                char == ',' || char == '.' || char == '\'' || char == '’' -> Unit
                else -> return null
            }
        }
    }

    if (normalizedNumber.isBlank() || normalizedNumber == ".") return null
    val normalized = if (sign != null) "$sign$normalizedNumber" else normalizedNumber

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
