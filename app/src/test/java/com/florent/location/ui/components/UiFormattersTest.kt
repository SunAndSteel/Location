package com.florent.location.ui.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UiFormattersTest {

    @Test
    fun parseEuroInputToCents_parsesFrenchNumber() {
        assertEquals(123_456L, parseEuroInputToCents("1 234,56"))
    }

    @Test
    fun parseEuroInputToCents_parsesFormattedValueWithCurrencySymbol() {
        assertEquals(123_456L, parseEuroInputToCents("1\u202F234,56 €"))
    }

    @Test
    fun parseEuroInputToCents_parsesDotThousandsAndCommaDecimals() {
        assertEquals(123_456L, parseEuroInputToCents("1.234,56"))
    }

    @Test
    fun parseEuroInputToCents_treatsSingleDotWithThreeTrailingDigitsAsThousandsSeparator() {
        assertEquals(123_400L, parseEuroInputToCents("1.234"))
    }

    @Test
    fun parseEuroInputToCents_treatsSingleCommaWithThreeTrailingDigitsAsThousandsSeparator() {
        assertEquals(123_400L, parseEuroInputToCents("1,234"))
    }

    @Test
    fun parseEuroInputToCents_returnsNullForInvalidInput() {
        assertNull(parseEuroInputToCents("abc"))
    }

    @Test
    fun parseEuroInputToCents_rejectsMultipleDecimalSeparators() {
        assertNull(parseEuroInputToCents("1,2,3"))
        assertNull(parseEuroInputToCents("12..34"))
    }
}
