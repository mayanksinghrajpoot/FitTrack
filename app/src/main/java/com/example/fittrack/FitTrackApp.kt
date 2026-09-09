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
        // Use a unique, compliant User-Agent so OpenStreetMap servers do not block requests (com.example is blocked by OSM policy)
        val osmConfig = org.osmdroid.config.Configuration.getInstance()
        osmConfig.userAgentValue = "FitTrack-AthleticTracker-Release-1.0"
        osmConfig.osmdroidBasePath = java.io.File(filesDir, "osm_base")
        osmConfig.osmdroidTileCache = java.io.File(cacheDir, "osm_tiles_v2")
    }
}
