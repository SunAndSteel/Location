package com.florent.location.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
        tableName = "housings",
        indices =
                [
                        Index(value = ["city"]),
                        Index(
                                value = ["address"],
                                unique = true
                        ) // optionnel : évite doublons exacts
                ]
)
data class HousingEntity(
        @PrimaryKey(autoGenerate = true) val id: Long = 0L,
        val city: String,
        val address: String,
        val defaultRentCents: Long = 0L,
        val defaultChargesCents: Long = 0L,
        val depositCents: Long = 0L,
        val mailboxLabel: String? = null,
        val meterGas: String? = null,
        val meterElectricity: String? = null,
        val meterWater: String? = null,
        val peb: String? = null, // ex: "C", ou "PEB C - 2021"
        val buildingLabel: String? = null // ex: "Bâtiment A"
)
