package com.example.fittrack.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.fittrack.domain.model.LocationPoint

/**
 * SQLite Entity table representing a completed fitness run/workout.
 */
@Entity(tableName = "runs")
data class RunEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val timestamp: Long = System.currentTimeMillis(),
    val durationMillis: Long = 0L,
    val distanceMeters: Float = 0f,
    val avgSpeedKmh: Float = 0f,
    val caloriesBurned: Int = 0,
    val locationPoints: List<LocationPoint> = emptyList()
)
