package com.florent.location.domain.model

data class Address(
    val street: String,
    val number: String,
    val box: String? = null,
    val zipCode: String,
    val city: String,
    val country: String = "BE"
) {
    fun fullString(): String {
        val head = listOfNotNull(
            "$number $street".trim(),
            box?.let { "Bte $it" }
        )
            .filter { it.isNotBlank() }
            .joinToString(" ")
        val tail = listOfNotNull(
            zipCode.takeIf { it.isNotBlank() },
            city.takeIf { it.isNotBlank() }
        ).joinToString(" ")
        return listOfNotNull(
            head.takeIf { it.isNotBlank() },
            tail.takeIf { it.isNotBlank() }
        ).joinToString(" - ")
    }
}
