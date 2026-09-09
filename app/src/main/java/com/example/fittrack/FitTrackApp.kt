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

        // OSMDroid Configuration: must call load() before any MapView is created.
        val prefs = getSharedPreferences("osmdroid_prefs", MODE_PRIVATE)
        val osmConfig = org.osmdroid.config.Configuration.getInstance()
        osmConfig.load(this, prefs)
        osmConfig.userAgentValue = "FitTrack-AthleticTracker/1.0"
        osmConfig.osmdroidBasePath = java.io.File(filesDir, "osm_base_v4")
        osmConfig.osmdroidTileCache = java.io.File(cacheDir, "carto_tiles_v1")

        // Clean up old blocked tile caches from previous OSM tile server attempts
        listOf("osm_tiles_v2", "osm_tiles_v3", "osm_base", "osm_base_v3").forEach { oldDir ->
            java.io.File(cacheDir, oldDir).deleteRecursively()
            java.io.File(filesDir, oldDir).deleteRecursively()
        }
    }
}
