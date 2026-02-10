package com.florent.location.data.sync

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class KeyRow(
    @SerialName("remote_id") val remoteId: String,
    @SerialName("user_id") val userId: String,

    @SerialName("housing_remote_id") val housingRemoteId: String,

    @SerialName("type") val type: String,
    @SerialName("device_label") val deviceLabel: String? = null,
    @SerialName("handed_over_epoch_day") val handedOverEpochDay: Long,

    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class IndexationEventRow(
    @SerialName("remote_id") val remoteId: String,
    @SerialName("user_id") val userId: String,

    @SerialName("lease_remote_id") val leaseRemoteId: String,

    @SerialName("applied_epoch_day") val appliedEpochDay: Long,
    @SerialName("base_rent_cents") val baseRentCents: Long,
    @SerialName("index_percent") val indexPercent: Double,
    @SerialName("new_rent_cents") val newRentCents: Long,

    @SerialName("updated_at") val updatedAt: String? = null
)