package com.example.gamezone.data.local

import androidx.room.TypeConverter

/**
 * Provides basic conversions for Room entities that contain collection fields.
 */
class Converters {
    private val separator = "|"

    @TypeConverter
    fun fromGenres(genres: List<String>?): String = genres?.joinToString(separator) ?: ""

    @TypeConverter
    fun toGenres(serialized: String): List<String> =
        if (serialized.isBlank()) emptyList() else serialized.split(separator)
}
