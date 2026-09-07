package com.example.fittrack.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fittrack.data.local.RunEntity
import com.example.fittrack.data.repository.RunRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel managing the full workout history list.
 */
class RunHistoryViewModel(private val repository: RunRepository) : ViewModel() {

    val runs: StateFlow<List<RunEntity>> = repository.allRuns.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun deleteRun(run: RunEntity) {
        viewModelScope.launch {
            repository.deleteRun(run)
        }
    }

    class Factory(private val repository: RunRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RunHistoryViewModel(repository) as T
        }
    }
}
