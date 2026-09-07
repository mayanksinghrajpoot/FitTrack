package com.example.fittrack.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fittrack.data.local.RunEntity
import com.example.fittrack.data.repository.RunRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val totalDistanceMeters: Float = 0f,
    val totalDurationMillis: Long = 0L,
    val totalCaloriesBurned: Int = 0,
    val totalRunCount: Int = 0,
    val recentRuns: List<RunEntity> = emptyList(),
    val isLoading: Boolean = false
)

/**
 * ViewModel for HomeScreen managing dashboard metrics and recent runs via Kotlin Coroutines and StateFlow.
 */
class HomeViewModel(private val repository: RunRepository) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        repository.totalDistanceMeters,
        repository.totalDurationMillis,
        repository.totalCaloriesBurned,
        repository.totalRunCount,
        repository.allRuns
    ) { distance, duration, calories, count, runs ->
        HomeUiState(
            totalDistanceMeters = distance ?: 0f,
            totalDurationMillis = duration ?: 0L,
            totalCaloriesBurned = calories ?: 0,
            totalRunCount = count,
            recentRuns = runs.take(5) // Show top 5 runs on dashboard
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(isLoading = true)
    )

    fun deleteRun(run: RunEntity) {
        viewModelScope.launch {
            repository.deleteRun(run)
        }
    }

    class Factory(private val repository: RunRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(repository) as T
        }
    }
}
