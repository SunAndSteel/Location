package com.florent.location.data.db

import androidx.room.TypeConverter
import com.florent.location.domain.model.PebRating

class PebRatingConverters {
    @TypeConverter
    fun fromPebRating(value: PebRating): String = value.name

    @TypeConverter
    fun toPebRating(value: String): PebRating =
        runCatching { PebRating.valueOf(value) }.getOrDefault(PebRating.UNKNOWN)
}
