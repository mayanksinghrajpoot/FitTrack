package com.example.fittrack.data.repository

import com.example.fittrack.data.local.RunDao
import com.example.fittrack.data.local.RunEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Repository layer acting as the single source of truth for Run data.
 * Executes Room SQLite queries asynchronously via Kotlin Coroutines.
 */
class RunRepository(private val runDao: RunDao) {

    val allRuns: Flow<List<RunEntity>> = runDao.getAllRuns()
    val totalDistanceMeters: Flow<Float?> = runDao.getTotalDistanceMeters()
    val totalDurationMillis: Flow<Long?> = runDao.getTotalDurationMillis()
    val totalCaloriesBurned: Flow<Int?> = runDao.getTotalCaloriesBurned()
    val totalRunCount: Flow<Int> = runDao.getTotalRunCount()

    suspend fun insertRun(run: RunEntity): Long = withContext(Dispatchers.IO) {
        runDao.insertRun(run)
    }

    suspend fun deleteRun(run: RunEntity) = withContext(Dispatchers.IO) {
        runDao.deleteRun(run)
    }

    suspend fun getRunById(runId: Long): RunEntity? = withContext(Dispatchers.IO) {
        runDao.getRunById(runId)
    }
}
