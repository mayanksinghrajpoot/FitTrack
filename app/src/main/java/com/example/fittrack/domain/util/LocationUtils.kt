package com.example.fittrack.domain.util

import android.location.Location
import com.example.fittrack.domain.model.LocationPoint

object LocationUtils {

    /**
     * Calculates the total distance covered in meters across a list of GPS points.
     */
    fun calculateTotalDistance(points: List<LocationPoint>): Float {
        if (points.size < 2) return 0f
        var totalDistance = 0f
        val result = FloatArray(1)

        for (i in 0 until points.size - 1) {
            val start = points[i]
            val end = points[i + 1]
            Location.distanceBetween(
                start.latitude,
                start.longitude,
                end.latitude,
                end.longitude,
                result
            )
            totalDistance += result[0]
        }
        return totalDistance
    }

    /**
     * Calculates average pace in seconds per kilometer.
     */
    fun calculateAveragePace(durationMillis: Long, distanceMeters: Float): Long {
        if (distanceMeters <= 0f || durationMillis <= 0L) return 0L
        val distanceKm = distanceMeters / 1000f
        val totalSeconds = durationMillis / 1000L
        return (totalSeconds / distanceKm).toLong()
    }

    /**
     * Estimates calories burned based on standard MET (Metabolic Equivalent of Task)
     * Assuming an average weight of 70kg for running/walking.
     */
    fun calculateCaloriesBurned(distanceMeters: Float, durationMillis: Long): Int {
        val distanceKm = distanceMeters / 1000f
        // Standard rule of thumb: ~60-70 kcal per kilometer for average runner
        return (distanceKm * 65f).toInt()
    }

    /**
     * Queries all active system location providers (GPS, Network, Passive) to return the best last known location.
     */
    @android.annotation.SuppressLint("MissingPermission")
    fun getBestLastKnownLocation(context: android.content.Context): LocationPoint? {
        val locationManager = context.getSystemService(android.content.Context.LOCATION_SERVICE) as? android.location.LocationManager ?: return null
        try {
            val providers = locationManager.getProviders(true)
            var bestLocation: Location? = null
            for (provider in providers) {
                val l = locationManager.getLastKnownLocation(provider) ?: continue
                if (bestLocation == null || l.time > bestLocation.time) {
                    bestLocation = l
                }
            }
            if (bestLocation != null) {
                return LocationPoint(
                    latitude = bestLocation.latitude,
                    longitude = bestLocation.longitude,
                    altitude = bestLocation.altitude,
                    timestamp = bestLocation.time
                )
            }
        } catch (_: SecurityException) {}
        return null
    }
}
