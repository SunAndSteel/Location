package com.florent.location.ui.components

import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val displayLocale: Locale = Locale.FRANCE

fun formatCurrency(cents: Long): String {
    val formatter = NumberFormat.getCurrencyInstance(displayLocale)
    return formatter.format(cents / 100.0)
}

fun formatEpochDay(epochDay: Long): String {
    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", displayLocale)
    return LocalDate.ofEpochDay(epochDay).format(formatter)
}
