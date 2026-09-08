package com.example.fittrack.ui.screens.active

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@Composable
fun ActiveTrackingScreen(
    viewModel: ActiveTrackingViewModel,
    onRunFinished: () -> Unit,
    onCloseClick: () -> Unit
) {
    val context = LocalContext.current
    val trackingState by viewModel.trackingState.collectAsState()

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

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            latLngPoints.lastOrNull() ?: LatLng(28.6139, 77.2090),
            17f
        )
    }

    // Center camera on actual device location immediately
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
        } catch (e: SecurityException) {
            // Permission missing
        }
    }

    // Follow user smoothly as coordinates stream in
    LaunchedEffect(latLngPoints.lastOrNull()) {
        latLngPoints.lastOrNull()?.let { lastPoint ->
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLng(lastPoint),
                600
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Live Google Map View with dynamic Red Polyline path
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = hasLocationPermission
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                compassEnabled = true,
                myLocationButtonEnabled = hasLocationPermission
            )
        ) {
            // Draw Athletic Crimson Red Polyline path when 2 or more points exist
            if (latLngPoints.size >= 2) {
                Polyline(
                    points = latLngPoints,
                    color = MapRouteRed,
                    width = 16f
                )
            }

            // Start Position Marker
            latLngPoints.firstOrNull()?.let { startPoint ->
                Marker(
                    state = MarkerState(position = startPoint),
                    title = "Start Point",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                )
            }

            // Current Position Marker & Pulse Circle
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
                    radius = 8.0, // 8 meters pulse circle
                    fillColor = CrimsonRed.copy(alpha = 0.35f),
                    strokeColor = CrimsonRed,
                    strokeWidth = 3f
                )
            }
        }

        // Top Controls: Discard / Close Button, Test Walk Simulation Chip & Status Badge
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
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

            // Simulation Toggle: Tap to test red line drawing indoors or on emulator
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = PureWhite,
                shadowElevation = 3.dp,
                border = BorderStroke(1.dp, CrimsonRed.copy(alpha = 0.4f)),
                modifier = Modifier.clickable {
                    viewModel.toggleSimulation(context)
                }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsRun,
                        contentDescription = null,
                        tint = CrimsonRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Simulate Walk",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = CrimsonRed
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
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (trackingState.isPaused) Color(0xFFF59E0B) else CrimsonRed)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (trackingState.isPaused) "PAUSED" else "LIVE",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (trackingState.isPaused) Color(0xFFB45309) else CrimsonRed
                    )
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
