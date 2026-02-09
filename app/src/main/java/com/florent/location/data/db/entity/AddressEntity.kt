package com.florent.location.data.db.entity

data class AddressEntity(
    val street: String,
    val number: String,
    val box: String? = null,
    val zipCode: String,
    val city: String,
    val country: String = "BE"
)
