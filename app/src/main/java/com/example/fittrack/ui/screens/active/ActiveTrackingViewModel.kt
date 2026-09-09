package com.example.fittrack.ui.screens.active

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fittrack.data.local.RunEntity
import com.example.fittrack.data.repository.RunRepository
import com.example.fittrack.domain.model.TrackingState
import com.example.fittrack.service.TrackingService
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel managing the active workout session.
 * Coordinates between the Foreground TrackingService, UI, and Room Database.
 */
class ActiveTrackingViewModel(private val repository: RunRepository) : ViewModel() {

    val trackingState: StateFlow<TrackingState> = TrackingService.trackingState

    fun startOrResumeTracking(context: Context) {
        val intent = Intent(context, TrackingService::class.java).apply {
            action = TrackingService.ACTION_START_OR_RESUME
        }
        ContextCompat.startForegroundService(context, intent)
    }

    fun pauseTracking(context: Context) {
        val intent = Intent(context, TrackingService::class.java).apply {
            action = TrackingService.ACTION_PAUSE
        }
        context.startService(intent)
    }

    fun toggleSimulation(context: Context) {
        if (TrackingService.serviceInstance != null) {
            TrackingService.toggleSimulationDirectly()
        } else {
            val intent = Intent(context, TrackingService::class.java).apply {
                action = TrackingService.ACTION_TOGGLE_SIMULATION
            }
            ContextCompat.startForegroundService(context, intent)
        }
    }

    fun finishAndSaveRun(context: Context, onSaved: () -> Unit) {
        val currentState = trackingState.value

        // Save if any movement or duration recorded
        if (currentState.durationMillis > 2000L || currentState.distanceMeters > 2f) {
            val runEntity = RunEntity(
                timestamp = System.currentTimeMillis(),
                durationMillis = currentState.durationMillis,
                distanceMeters = currentState.distanceMeters,
                avgSpeedKmh = if (currentState.durationMillis > 0) {
                    (currentState.distanceMeters / (currentState.durationMillis / 1000f)) * 3.6f
                } else 0f,
                caloriesBurned = currentState.caloriesBurned,
                locationPoints = currentState.locationPoints
            )

            viewModelScope.launch {
                repository.insertRun(runEntity)
                stopServiceAndReset(context)
                onSaved()
            }
        } else {
            stopServiceAndReset(context)
            onSaved()
        }
    }

    fun discardRun(context: Context, onDiscarded: () -> Unit) {
        stopServiceAndReset(context)
        onDiscarded()
    }

    private fun stopServiceAndReset(context: Context) {
        val intent = Intent(context, TrackingService::class.java).apply {
            action = TrackingService.ACTION_STOP
        }
        context.startService(intent)
        TrackingService.resetTrackingState()
    }

    class Factory(private val repository: RunRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ActiveTrackingViewModel(repository) as T
        }
    }
}
