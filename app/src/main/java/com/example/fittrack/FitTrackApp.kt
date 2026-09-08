package com.example.fittrack

import android.app.Application
import com.example.fittrack.data.local.RunDatabase
import com.example.fittrack.data.repository.RunRepository

/**
 * Base Application class for FitTrack initializing Room Database and Repository.
 */
class FitTrackApp : Application() {

    val database by lazy { RunDatabase.getDatabase(this) }
    val repository by lazy { RunRepository(database.runDao()) }

    override fun onCreate() {
        super.onCreate()
        org.osmdroid.config.Configuration.getInstance().userAgentValue = packageName
    }
}
