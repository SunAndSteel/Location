package com.florent.location.data.sync

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LeaseRow(
    @SerialName("remote_id") val remoteId: String,
    @SerialName("user_id") val userId: String,

    @SerialName("housing_remote_id") val housingRemoteId: String,
    @SerialName("tenant_remote_id") val tenantRemoteId: String,

    @SerialName("start_date_epoch_day") val startDateEpochDay: Long,
    @SerialName("end_date_epoch_day") val endDateEpochDay: Long? = null,

    @SerialName("rent_cents") val rentCents: Long,
    @SerialName("charges_cents") val chargesCents: Long,
    @SerialName("deposit_cents") val depositCents: Long = 0,

    @SerialName("rent_due_day_of_month") val rentDueDayOfMonth: Int = 1,
    @SerialName("index_anniversary_epoch_day") val indexAnniversaryEpochDay: Long? = null,

    @SerialName("rent_overridden") val rentOverridden: Boolean = false,
    @SerialName("charges_overridden") val chargesOverridden: Boolean = false,
    @SerialName("deposit_overridden") val depositOverridden: Boolean = false,

    @SerialName("housing_rent_cents_snapshot") val housingRentCentsSnapshot: Long = 0,
    @SerialName("housing_charges_cents_snapshot") val housingChargesCentsSnapshot: Long = 0,
    @SerialName("housing_deposit_cents_snapshot") val housingDepositCentsSnapshot: Long = 0,

    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)
