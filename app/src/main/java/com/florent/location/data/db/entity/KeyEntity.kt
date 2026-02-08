package com.florent.location.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
        tableName = "keys",
        foreignKeys =
                [
                        ForeignKey(
                                entity = HousingEntity::class,
                                parentColumns = ["id"],
                                childColumns = ["housingId"],
                                onDelete =
                                        ForeignKey.CASCADE // si tu supprimes un logement, tu supprimes
                                // les clés associées
                                )],
        indices = [Index(value = ["housingId"])]
)
data class KeyEntity(
        @PrimaryKey(autoGenerate = true) val id: Long = 0L,
        val housingId: Long,
        val type: String, // "Clé", "Badge", "Télécommande", etc.
        val deviceLabel: String? = null, // "Garage", "Portail", ...
        val handedOverEpochDay: Long // LocalDate.toEpochDay()
)
