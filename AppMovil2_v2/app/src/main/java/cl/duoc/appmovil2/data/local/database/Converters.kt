package cl.duoc.appmovil2.data.local.database

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromGenderList(genders: List<String>): String {
        return genders.joinToString(separator = "|")
    }

    @TypeConverter
    fun toGenderList(serialized: String): List<String> {
        if (serialized.isBlank()) {
            return emptyList()
        }
        return serialized.split("|").map { it.trim() }.filter { it.isNotEmpty() }
    }
}
