package com.example.fittrack.data.local

import androidx.room.TypeConverter
import com.example.fittrack.domain.model.LocationPoint
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * TypeConverter to serialize List<LocationPoint> to and from JSON string in SQLite.
 */
class Converters {
    private val gson = Gson()
    private val locationListType = object : TypeToken<List<LocationPoint>>() {}.type

    @TypeConverter
    fun fromLocationPointsList(points: List<LocationPoint>?): String {
        if (points.isNullOrEmpty()) return "[]"
        return gson.toJson(points)
    }

    @TypeConverter
    fun toLocationPointsList(json: String?): List<LocationPoint> {
        if (json.isNullOrEmpty()) return emptyList()
        return try {
            gson.fromJson(json, locationListType) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
