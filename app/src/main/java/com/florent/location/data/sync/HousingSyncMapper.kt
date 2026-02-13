package com.florent.location.data.sync

import com.florent.location.data.db.entity.AddressEntity
import com.florent.location.data.db.entity.HousingEntity
import com.florent.location.domain.model.PebRating
import java.time.Instant

fun HousingEntity.toRow(userId: String): HousingRow = HousingRow(
    remoteId = remoteId,
    userId = userId,

    addrStreet = address.street,
    addrNumber = address.number,
    addrBox = address.box,
    addrZipCode = address.zipCode,
    addrCity = address.city,
    addrCountry = address.country,

    isArchived = isArchived,

    rentCents = rentCents,
    chargesCents = chargesCents,
    depositCents = depositCents,

    meterGasId = meterGasId,
    meterElectricityId = meterElectricityId,
    meterWaterId = meterWaterId,

    mailboxLabel = mailboxLabel,
    pebRating = pebRating.name,
    pebDate = pebDate,

    buildingLabel = buildingLabel,
    internalNote = internalNote,
    createdAt = Instant.ofEpochMilli(createdAt).toString()
)

fun HousingRow.toEntityPreservingLocalId(
    localId: Long,
    existingCreatedAtMillis: Long? = null,
    nowMillis: Long = System.currentTimeMillis()
): HousingEntity {
    val serverCreatedAtMillis = parseServerEpochMillis(createdAt)
    val serverUpdatedAtMillis = parseServerEpochMillis(updatedAt)
    val serverUpdatedAtMillisCursor = parseServerEpochMillis(updatedAt)

    return HousingEntity(
        id = localId,
        remoteId = remoteId,
        address = AddressEntity(
            street = addrStreet,
            number = addrNumber,
            box = addrBox,
            zipCode = addrZipCode,
            city = addrCity,
            country = addrCountry
        ),
        createdAt = serverCreatedAtMillis ?: existingCreatedAtMillis ?: nowMillis,
        updatedAt = serverUpdatedAtMillis ?: nowMillis,
        isArchived = isArchived,
        rentCents = rentCents,
        chargesCents = chargesCents,
        depositCents = depositCents,
        meterGasId = meterGasId,
        meterElectricityId = meterElectricityId,
        meterWaterId = meterWaterId,
        mailboxLabel = mailboxLabel,
        pebRating = runCatching { PebRating.valueOf(pebRating) }.getOrDefault(PebRating.UNKNOWN),
        pebDate = pebDate,
        buildingLabel = buildingLabel,
        internalNote = internalNote,
        dirty = false,
        serverUpdatedAtEpochSeconds = serverUpdatedAtMillisCursor
    )
}
