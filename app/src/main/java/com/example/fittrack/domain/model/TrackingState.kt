package com.example.fittrack.domain.model

/**
 * State of the Foreground Tracking Service and UI recording.
 */
data class TrackingState(
    val isTracking: Boolean = false,
    val isPaused: Boolean = false,
    val durationMillis: Long = 0L,
    val distanceMeters: Float = 0f,
    val currentSpeedKmh: Float = 0f,
    val averagePaceSecondsPerKm: Long = 0L,
    val caloriesBurned: Int = 0,
    val isSimulating: Boolean = false,
    val locationPoints: List<LocationPoint> = emptyList()
)
