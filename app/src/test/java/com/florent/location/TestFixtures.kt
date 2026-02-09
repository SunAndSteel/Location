package com.florent.location

import com.florent.location.domain.model.Address
import com.florent.location.domain.model.Housing

fun sampleAddress(
    street: String = "Rue de la Paix",
    number: String = "1",
    zipCode: String = "1000",
    city: String = "Bruxelles",
    country: String = "BE"
): Address = Address(
    street = street,
    number = number,
    zipCode = zipCode,
    city = city,
    country = country
)

fun sampleHousing(
    id: Long = 0L,
    city: String = "Bruxelles",
    remoteId: String = "remote-$id",
    rentCents: Long = 0L,
    chargesCents: Long = 0L,
    depositCents: Long = 0L
): Housing = Housing(
    id = id,
    remoteId = remoteId,
    address = sampleAddress(city = city),
    rentCents = rentCents,
    chargesCents = chargesCents,
    depositCents = depositCents
)
