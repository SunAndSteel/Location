package com.florent.location.data.sync

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HousingRow(
    @SerialName("remote_id") val remoteId: String,
    @SerialName("user_id") val userId: String,

    @SerialName("addr_street") val addrStreet: String,
    @SerialName("addr_number") val addrNumber: String,
    @SerialName("addr_box") val addrBox: String? = null,
    @SerialName("addr_zip_code") val addrZipCode: String,
    @SerialName("addr_city") val addrCity: String,
    @SerialName("addr_country") val addrCountry: String = "BE",

    @SerialName("is_archived") val isArchived: Boolean = false,

    @SerialName("rent_cents") val rentCents: Long = 0,
    @SerialName("charges_cents") val chargesCents: Long = 0,
    @SerialName("deposit_cents") val depositCents: Long = 0,

    @SerialName("meter_gas_id") val meterGasId: String? = null,
    @SerialName("meter_electricity_id") val meterElectricityId: String? = null,
    @SerialName("meter_water_id") val meterWaterId: String? = null,

    @SerialName("mailbox_label") val mailboxLabel: String? = null,

    @SerialName("peb_rating") val pebRating: String = "UNKNOWN",
    @SerialName("peb_date") val pebDate: String? = null,

    @SerialName("building_label") val buildingLabel: String? = null,
    @SerialName("internal_note") val internalNote: String? = null,

    // timestamptz ISO
    @SerialName("updated_at") val updatedAt: String? = null
)
