package com.example.fittrack.ui.screens.active

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color as AndroidColor
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.fittrack.domain.util.TimeUtils
import com.example.fittrack.ui.theme.CardBorderColor
import com.example.fittrack.ui.theme.CrimsonRed
import com.example.fittrack.ui.theme.CrimsonRedLight
import com.example.fittrack.ui.theme.MapRouteRed
import com.example.fittrack.ui.theme.PureWhite
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.util.MapTileIndex
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker as OsmMarker
import org.osmdroid.views.overlay.Polyline as OsmPolyline

enum class MapEngine {
    OPEN_STREET_MAP,
    GOOGLE_MAPS
}

@Composable
fun ActiveTrackingScreen(
    viewModel: ActiveTrackingViewModel,
    onRunFinished: () -> Unit,
    onCloseClick: () -> Unit
) {
    val context = LocalContext.current
    val trackingState by viewModel.trackingState.collectAsState()

    // Default to Google Maps since user has a working API key
    var selectedMapEngine by remember { mutableStateOf(MapEngine.GOOGLE_MAPS) }

    val hasLocationPermission = remember {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Start tracking when screen launches if not already running
    LaunchedEffect(Unit) {
        if (!trackingState.isTracking) {
            viewModel.startOrResumeTracking(context)
        }
    }

    val latLngPoints = remember(trackingState.locationPoints) {
        trackingState.locationPoints.map { LatLng(it.latitude, it.longitude) }
    }

    val geoPoints = remember(trackingState.locationPoints) {
        trackingState.locationPoints.map { GeoPoint(it.latitude, it.longitude) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (selectedMapEngine == MapEngine.GOOGLE_MAPS) {
            // Google Maps Compose: Fully working with user's API key
            GoogleMapRenderer(
                latLngPoints = latLngPoints,
                hasLocationPermission = hasLocationPermission,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // OpenStreetMap (OSMDroid): Free fallback with no API key needed
            OpenStreetMapRenderer(
                geoPoints = geoPoints,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Top Controls: Discard Button, Map Engine Switcher, Simulation Toggle & Live Badge
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 44.dp, start = 14.dp, end = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Discard Run Button
                Surface(
                    shape = CircleShape,
                    color = PureWhite,
                    shadowElevation = 4.dp,
                    border = BorderStroke(1.dp, CardBorderColor)
                ) {
                    IconButton(onClick = { viewModel.discardRun(context, onCloseClick) }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Discard Run",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Map Engine Switcher: Google vs OSM
                Surface(
                    onClick = {
                        selectedMapEngine = if (selectedMapEngine == MapEngine.GOOGLE_MAPS) {
                            MapEngine.OPEN_STREET_MAP
                        } else {
                            MapEngine.GOOGLE_MAPS
                        }
                    },
                    shape = RoundedCornerShape(20.dp),
                    color = PureWhite,
                    shadowElevation = 3.dp,
                    border = BorderStroke(1.dp, CardBorderColor)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = null,
                            tint = CrimsonRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (selectedMapEngine == MapEngine.GOOGLE_MAPS) "Map: Google" else "Map: OSM",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Simulate Walk Button with Live Active State
                Surface(
                    onClick = { viewModel.toggleSimulation(context) },
                    shape = RoundedCornerShape(20.dp),
                    color = if (trackingState.isSimulating) CrimsonRed else PureWhite,
                    shadowElevation = 4.dp,
                    border = BorderStroke(1.dp, CrimsonRed)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (trackingState.isSimulating) Icons.Default.Stop else Icons.Default.DirectionsRun,
                            contentDescription = null,
                            tint = if (trackingState.isSimulating) PureWhite else CrimsonRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (trackingState.isSimulating) "Stop Walk" else "Simulate Walk",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (trackingState.isSimulating) PureWhite else CrimsonRed
                        )
                    }
                }

                // Live Pulse Badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (trackingState.isPaused) Color(0xFFFEF3C7) else CrimsonRedLight,
                    border = BorderStroke(1.dp, if (trackingState.isPaused) Color(0xFFF59E0B) else CrimsonRed)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (trackingState.isPaused) Color(0xFFF59E0B) else CrimsonRed)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (trackingState.isPaused) "PAUSED" else "LIVE",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (trackingState.isPaused) Color(0xFFB45309) else CrimsonRed
                        )
                    }
                }
            }

            // Indoor GPS notice banner when awaiting satellite fix
            AnimatedVisibility(
                visible = trackingState.locationPoints.size < 2 && !trackingState.isSimulating,
                modifier = Modifier.padding(top = 10.dp)
            ) {
                Surface(
                    onClick = { viewModel.toggleSimulation(context) },
                    shape = RoundedCornerShape(12.dp),
                    color = PureWhite,
                    shadowElevation = 4.dp,
                    border = BorderStroke(1.dp, CrimsonRed.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsRun,
                            contentDescription = null,
                            tint = CrimsonRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Indoor? Tap to simulate walk & draw route!",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = CrimsonRed,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Bottom HUD Card with Metrics & Action Controls
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            border = BorderStroke(1.dp, CardBorderColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Distance Highlight
                Text(
                    text = "DISTANCE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = String.format("%.2f", trackingState.distanceMeters / 1000f),
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 46.sp,
                            fontWeight = FontWeight.Black
                        ),
                        color = CrimsonRed
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "KM",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = CrimsonRed,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Stats Row: Duration, Avg Pace, Calories
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    WorkoutMetricItem(
                        icon = Icons.Default.Timer,
                        label = "TIME",
                        value = TimeUtils.formatDuration(trackingState.durationMillis)
                    )
                    WorkoutMetricItem(
                        icon = Icons.Default.Speed,
                        label = "PACE",
                        value = TimeUtils.formatPace(trackingState.averagePaceSecondsPerKm)
                    )
                    WorkoutMetricItem(
                        icon = Icons.Default.LocalFireDepartment,
                        label = "CALORIES",
                        value = "${trackingState.caloriesBurned}"
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Controls: Pause/Resume & Finish Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Pause / Resume Button
                    Surface(
                        onClick = {
                            if (trackingState.isPaused) {
                                viewModel.startOrResumeTracking(context)
                            } else {
                                viewModel.pauseTracking(context)
                            }
                        },
                        shape = CircleShape,
                        color = if (trackingState.isPaused) CrimsonRed else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(64.dp),
                        shadowElevation = 2.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (trackingState.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                contentDescription = if (trackingState.isPaused) "Resume" else "Pause",
                                tint = if (trackingState.isPaused) PureWhite else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(24.dp))

                    // Finish & Save Run Button
                    Surface(
                        onClick = { viewModel.finishAndSaveRun(context, onRunFinished) },
                        shape = CircleShape,
                        color = CrimsonRed,
                        modifier = Modifier.size(64.dp),
                        shadowElevation = 4.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Done,
                                contentDescription = "Finish Run",
                                tint = PureWhite,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * OpenStreetMap (OSMDroid) Renderer:
 * Uses Carto CDN tiles instead of tile.openstreetmap.org (which blocks com.example.* apps).
 * Carto Voyager tiles are free, fast, and render beautiful street-level detail.
 */
@Composable
private fun OpenStreetMapRenderer(
    geoPoints: List<GeoPoint>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Carto Voyager tile source: free CDN with no User-Agent blocking
    val cartoTileSource = remember {
        object : OnlineTileSourceBase(
            "CartoDB_Voyager",
            0, 20, 256, ".png",
            arrayOf(
                "https://a.basemaps.cartocdn.com/rastertiles/voyager/",
                "https://b.basemaps.cartocdn.com/rastertiles/voyager/",
                "https://c.basemaps.cartocdn.com/rastertiles/voyager/",
                "https://d.basemaps.cartocdn.com/rastertiles/voyager/"
            )
        ) {
            override fun getTileURLString(pMapTileIndex: Long): String {
                val zoom = MapTileIndex.getZoom(pMapTileIndex)
                val x = MapTileIndex.getX(pMapTileIndex)
                val y = MapTileIndex.getY(pMapTileIndex)
                return "${getBaseUrl()}$zoom/$x/$y$mImageFilenameEnding"
            }
        }
    }

    // Remember a single MapView instance across recompositions
    val mapView = remember {
        // Configure OSMDroid before MapView creation
        val prefs = context.getSharedPreferences("osmdroid_prefs", Context.MODE_PRIVATE)
        val osmConfig = org.osmdroid.config.Configuration.getInstance()
        osmConfig.load(context, prefs)
        osmConfig.userAgentValue = "FitTrack-AthleticTracker/1.0"

        // Use a fresh tile cache to avoid stale 403 tiles from previous tile servers
        val tileCache = java.io.File(context.cacheDir, "carto_tiles_v1")
        osmConfig.osmdroidBasePath = java.io.File(context.filesDir, "osm_base_v4")
        osmConfig.osmdroidTileCache = tileCache

        // Delete old blocked tile caches from previous versions
        listOf("osm_tiles_v2", "osm_tiles_v3").forEach { oldDir ->
            java.io.File(context.cacheDir, oldDir).deleteRecursively()
        }

        MapView(context).apply {
            setTileSource(cartoTileSource)
            setMultiTouchControls(true)
            isTilesScaledToDpi = true
            controller.setZoom(17.5)
            controller.setCenter(GeoPoint(28.6139, 77.2090))
            onResume()
        }
    }

    // Proper lifecycle management
    DisposableEffect(Unit) {
        mapView.onResume()
        onDispose {
            mapView.onPause()
            mapView.onDetach()
        }
    }

    // Update overlays safely whenever geoPoints change
    LaunchedEffect(geoPoints.size) {
        try {
            val newOverlays = mutableListOf<org.osmdroid.views.overlay.Overlay>()

            if (geoPoints.size >= 2) {
                val polyline = OsmPolyline().apply {
                    outlinePaint.color = AndroidColor.parseColor("#E53935")
                    outlinePaint.strokeWidth = 14f
                    outlinePaint.isAntiAlias = true
                    outlinePaint.strokeCap = android.graphics.Paint.Cap.ROUND
                    outlinePaint.strokeJoin = android.graphics.Paint.Join.ROUND
                    setPoints(geoPoints.toList())
                }
                newOverlays.add(polyline)
            }

            geoPoints.firstOrNull()?.let { startPoint ->
                val startMarker = OsmMarker(mapView).apply {
                    position = startPoint
                    title = "Start"
                    setAnchor(OsmMarker.ANCHOR_CENTER, OsmMarker.ANCHOR_BOTTOM)
                }
                newOverlays.add(startMarker)
            }

            geoPoints.lastOrNull()?.let { currentPoint ->
                val currentMarker = OsmMarker(mapView).apply {
                    position = currentPoint
                    title = "Current"
                    setAnchor(OsmMarker.ANCHOR_CENTER, OsmMarker.ANCHOR_BOTTOM)
                }
                newOverlays.add(currentMarker)
                mapView.controller.animateTo(currentPoint)
            }

            mapView.overlays.clear()
            mapView.overlays.addAll(newOverlays)
            mapView.invalidate()
        } catch (_: Exception) {
            // Safety net for ConcurrentModificationException during rapid simulation updates
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { mapView }
    )
}

/**
 * Google Maps Compose Renderer
 */
@Composable
private fun GoogleMapRenderer(
    latLngPoints: List<LatLng>,
    hasLocationPermission: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            latLngPoints.lastOrNull() ?: LatLng(28.6139, 77.2090),
            17f
        )
    }

    LaunchedEffect(Unit) {
        try {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            fusedClient.lastLocation.addOnSuccessListener { loc ->
                if (loc != null && latLngPoints.isEmpty()) {
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(
                        LatLng(loc.latitude, loc.longitude),
                        17f
                    )
                }
            }
        } catch (_: SecurityException) {}
    }

    LaunchedEffect(latLngPoints.lastOrNull()) {
        latLngPoints.lastOrNull()?.let { lastPoint ->
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLng(lastPoint),
                600
            )
        }
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = MapProperties(isMyLocationEnabled = hasLocationPermission),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = false,
            compassEnabled = true,
            myLocationButtonEnabled = hasLocationPermission
        )
    ) {
        if (latLngPoints.size >= 2) {
            Polyline(
                points = latLngPoints,
                color = MapRouteRed,
                width = 16f
            )
        }

        latLngPoints.firstOrNull()?.let { startPoint ->
            Marker(
                state = MarkerState(position = startPoint),
                title = "Start Point",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
            )
        }

        latLngPoints.lastOrNull()?.let { currentPoint ->
            if (latLngPoints.size > 1) {
                Marker(
                    state = MarkerState(position = currentPoint),
                    title = "Current Position",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                )
            }
            Circle(
                center = currentPoint,
                radius = 8.0,
                fillColor = CrimsonRed.copy(alpha = 0.35f),
                strokeColor = CrimsonRed,
                strokeWidth = 3f
            )
        }
    }
}

@Composable
private fun WorkoutMetricItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
