package com.florent.location.data.sync

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TenantRow(
    @SerialName("remote_id") val remoteId: String,
    @SerialName("user_id") val userId: String,

    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String,
    @SerialName("phone") val phone: String? = null,
    @SerialName("email") val email: String? = null,
    @SerialName("status") val status: String = "ACTIVE",

    @SerialName("updated_at") val updatedAt: String? = null
)