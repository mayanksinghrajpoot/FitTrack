package com.example.fittrack.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Room Data Access Object (DAO) providing asynchronous SQLite operations.
 * Room natively supports Kotlin Coroutines Flow for real-time observable queries.
 */
@Dao
interface RunDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(run: RunEntity): Long

    @Delete
    suspend fun deleteRun(run: RunEntity)

    @Query("SELECT * FROM runs ORDER BY timestamp DESC")
    fun getAllRuns(): Flow<List<RunEntity>>

    @Query("SELECT * FROM runs WHERE id = :runId LIMIT 1")
    suspend fun getRunById(runId: Long): RunEntity?

    @Query("SELECT SUM(distanceMeters) FROM runs")
    fun getTotalDistanceMeters(): Flow<Float?>

    @Query("SELECT SUM(durationMillis) FROM runs")
    fun getTotalDurationMillis(): Flow<Long?>

    @Query("SELECT SUM(caloriesBurned) FROM runs")
    fun getTotalCaloriesBurned(): Flow<Int?>

    @Query("SELECT COUNT(*) FROM runs")
    fun getTotalRunCount(): Flow<Int>
}
