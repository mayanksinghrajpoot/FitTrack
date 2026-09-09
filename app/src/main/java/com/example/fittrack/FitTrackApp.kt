package com.example.fittrack

import android.app.Application
import com.example.fittrack.data.local.RunDatabase
import com.example.fittrack.data.repository.RunRepository

/**
 * Base Application class for FitTrack initializing Room Database, Repository, and OSMDroid.
 */
class FitTrackApp : Application() {

    val database by lazy { RunDatabase.getDatabase(this) }
    val repository by lazy { RunRepository(database.runDao()) }

    override fun onCreate() {
        super.onCreate()

        // OSMDroid MUST be configured with load() before any MapView is instantiated.
        // Without load(), tile providers ignore userAgentValue and show blank grid tiles.
        val prefs = getSharedPreferences("osmdroid_prefs", MODE_PRIVATE)
        val osmConfig = org.osmdroid.config.Configuration.getInstance()
        osmConfig.load(this, prefs)
        osmConfig.userAgentValue = "FitTrack-AthleticTracker/1.0"
        osmConfig.osmdroidBasePath = java.io.File(filesDir, "osm_base_v3")
        osmConfig.osmdroidTileCache = java.io.File(cacheDir, "osm_tiles_v3")
    }
}
