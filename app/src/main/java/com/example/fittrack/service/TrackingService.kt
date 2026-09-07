package com.example.fittrack.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.example.fittrack.MainActivity
import com.example.fittrack.R
import com.example.fittrack.domain.model.LocationPoint
import com.example.fittrack.domain.model.TrackingState
import com.example.fittrack.domain.util.LocationUtils
import com.example.fittrack.domain.util.TimeUtils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Foreground Service that handles continuous GPS tracking and time recording.
 * Maintains an active notification and emits real-time updates via StateFlow.
 */
class TrackingService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private var timerJob: Job? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var notificationManager: NotificationManager

    companion object {
        const val ACTION_START_OR_RESUME = "ACTION_START_OR_RESUME"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_STOP = "ACTION_STOP"

        const val NOTIFICATION_CHANNEL_ID = "fittrack_tracking_channel"
        const val NOTIFICATION_ID = 1001

        private val _trackingState = MutableStateFlow(TrackingState())
        val trackingState: StateFlow<TrackingState> = _trackingState.asStateFlow()

        /**
         * Clears tracking state when a run is saved or discarded.
         */
        fun resetTrackingState() {
            _trackingState.value = TrackingState()
        }
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        createNotificationChannel()
        setupLocationCallback()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_OR_RESUME -> {
                startForegroundServiceWithTracking()
            }
            ACTION_PAUSE -> {
                pauseTracking()
            }
            ACTION_STOP -> {
                stopTrackingService()
            }
        }
        return START_STICKY
    }

    private fun startForegroundServiceWithTracking() {
        val wasPaused = _trackingState.value.isPaused
        _trackingState.update {
            it.copy(isTracking = true, isPaused = false)
        }

        // Start Foreground Service with notification
        val notification = buildNotification("Starting run...")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        // Start location updates and timer
        startLocationUpdates()
        startTimer()
    }

    private fun pauseTracking() {
        _trackingState.update { it.copy(isPaused = true) }
        stopLocationUpdates()
        timerJob?.cancel()

        val notification = buildNotification("Run paused")
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun stopTrackingService() {
        _trackingState.update { it.copy(isTracking = false, isPaused = false) }
        stopLocationUpdates()
        timerJob?.cancel()

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (_trackingState.value.isTracking && !_trackingState.value.isPaused) {
                delay(1000L)
                _trackingState.update { current ->
                    val newDuration = current.durationMillis + 1000L
                    val avgPace = LocationUtils.calculateAveragePace(newDuration, current.distanceMeters)
                    val calories = LocationUtils.calculateCaloriesBurned(current.distanceMeters, newDuration)
                    current.copy(
                        durationMillis = newDuration,
                        averagePaceSecondsPerKm = avgPace,
                        caloriesBurned = calories
                    )
                }

                // Update notification every few seconds
                if ((_trackingState.value.durationMillis / 1000L) % 3L == 0L) {
                    val state = _trackingState.value
                    val distKm = String.format("%.2f km", state.distanceMeters / 1000f)
                    val timeStr = TimeUtils.formatDuration(state.durationMillis)
                    val content = "$distKm  |  $timeStr  |  ${state.caloriesBurned} kcal"
                    notificationManager.notify(NOTIFICATION_ID, buildNotification(content))
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            3000L // update interval: 3 seconds
        ).apply {
            setMinUpdateIntervalMillis(2000L)
            setMinUpdateDistanceMeters(2f) // update every 2 meters
        }.build()

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            // Handled in UI permissions
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun setupLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)
                if (!_trackingState.value.isTracking || _trackingState.value.isPaused) return

                result.locations.forEach { location ->
                    val newPoint = LocationPoint(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        altitude = location.altitude,
                        timestamp = location.time
                    )

                    _trackingState.update { current ->
                        val updatedList = current.locationPoints + newPoint
                        val totalDistance = LocationUtils.calculateTotalDistance(updatedList)
                        val speedKmh = location.speed * 3.6f // m/s to km/h
                        val avgPace = LocationUtils.calculateAveragePace(current.durationMillis, totalDistance)
                        val calories = LocationUtils.calculateCaloriesBurned(totalDistance, current.durationMillis)

                        current.copy(
                            distanceMeters = totalDistance,
                            currentSpeedKmh = speedKmh,
                            averagePaceSecondsPerKm = avgPace,
                            caloriesBurned = calories,
                            locationPoints = updatedList
                        )
                    }
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                getString(R.string.channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.channel_desc)
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(contentText: String): android.app.Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("FitTrack • Live Recording")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(getColor(R.color.primary_red))
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
        timerJob?.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
