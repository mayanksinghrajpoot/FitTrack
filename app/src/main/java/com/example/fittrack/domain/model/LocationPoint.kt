package com.example.fittrack.domain.model

/**
 * Domain model representing a single GPS coordinate point along a run path.
 */
data class LocationPoint(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)
