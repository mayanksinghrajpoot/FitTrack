package com.example.fittrack.domain.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object TimeUtils {

    /**
     * Formats milliseconds into a clean stopwatch timer string:
     * "MM:SS" or "HH:MM:SS" if duration exceeds one hour.
     */
    fun formatDuration(durationMillis: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(durationMillis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMillis) % 60

        return if (hours > 0) {
            String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        }
    }

    /**
     * Formats pace in seconds/km into "M'SS\" /km" (e.g. 5'30" /km)
     */
    fun formatPace(paceSecondsPerKm: Long): String {
        if (paceSecondsPerKm <= 0L || paceSecondsPerKm > 3600) return "--'--\""
        val minutes = paceSecondsPerKm / 60
        val seconds = paceSecondsPerKm % 60
        return String.format(Locale.getDefault(), "%d'%02d\"", minutes, seconds)
    }

    /**
     * Formats timestamp into readable date like "12 Oct 2024, 07:30 AM"
     */
    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
