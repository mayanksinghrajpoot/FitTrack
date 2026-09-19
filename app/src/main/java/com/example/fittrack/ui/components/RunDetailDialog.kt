package com.example.fittrack.ui.components

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocationOn
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.viewinterop.AndroidView
import com.example.fittrack.data.local.RunEntity
import com.example.fittrack.domain.util.LocationUtils
import com.example.fittrack.domain.util.TimeUtils
import com.example.fittrack.ui.theme.CardBorderColor
import com.example.fittrack.ui.theme.CrimsonRed
import com.example.fittrack.ui.theme.CrimsonRedLight
import com.example.fittrack.ui.theme.PureWhite
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.MapTileIndex
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker as OsmMarker
import org.osmdroid.views.overlay.Polyline as OsmPolyline

/**
 * Dialog displaying complete route track on map, start/end time frame,
 * and comprehensive workout statistics for a saved run entity.
 */
@Composable
fun RunDetailDialog(
    run: RunEntity,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val geoPoints = remember(run.locationPoints) {
        run.locationPoints.map { GeoPoint(it.latitude, it.longitude) }
    }

    val distanceKm = run.distanceMeters / 1000f
    val formattedDistance = String.format(java.util.Locale.getDefault(), "%.2f", distanceKm)
    val formattedDuration = TimeUtils.formatDuration(run.durationMillis)
    val formattedDate = TimeUtils.formatDate(run.timestamp)
    val timeRange = TimeUtils.formatTimeRange(run.timestamp, run.durationMillis)
    val avgPaceSeconds = LocationUtils.calculateAveragePace(run.durationMillis, run.distanceMeters)
    val formattedPace = TimeUtils.formatPace(avgPaceSeconds)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 24.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.background,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header with Title & Close Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(CrimsonRedLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsRun,
                                contentDescription = null,
                                tint = CrimsonRed,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = "Recorded Workout",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = formattedDate,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Map View Route Frame (Height 220.dp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    if (geoPoints.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No GPS route points recorded for this run.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        RouteMapView(
                            geoPoints = geoPoints,
                            modifier = Modifier.matchParentSize()
                        )
                    }
                }

                // Stats Content Padding
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Time Frame & Date Banner
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = CrimsonRed,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = formattedDate,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = null,
                                    tint = CrimsonRed,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Time Frame: $timeRange",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Distance Highlight
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        border = BorderStroke(1.dp, CardBorderColor)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "TOTAL DISTANCE",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = formattedDistance,
                                    style = MaterialTheme.typography.displayLarge.copy(
                                        fontSize = 40.sp,
                                        fontWeight = FontWeight.Black
                                    ),
                                    color = CrimsonRed
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "KM",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = CrimsonRed,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 4 Grid Metrics Cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DetailStatChip(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Timer,
                            title = "DURATION",
                            value = formattedDuration
                        )
                        DetailStatChip(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Speed,
                            title = "AVG SPEED",
                            value = String.format(java.util.Locale.getDefault(), "%.1f km/h", run.avgSpeedKmh)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DetailStatChip(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Speed,
                            title = "AVG PACE",
                            value = formattedPace
                        )
                        DetailStatChip(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.LocalFireDepartment,
                            title = "CALORIES",
                            value = "${run.caloriesBurned} kcal"
                        )
                    }

                    if (geoPoints.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${geoPoints.size} GPS waypoints recorded along route",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailStatChip(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = BorderStroke(1.dp, CardBorderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = CrimsonRed,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun RouteMapView(
    geoPoints: List<GeoPoint>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val cityMapTileSource = remember {
        object : OnlineTileSourceBase(
            "WikimediaCityMap",
            0, 19, 256, ".png",
            arrayOf("https://maps.wikimedia.org/osm-intl/")
        ) {
            override fun getTileURLString(pMapTileIndex: Long): String {
                val zoom = MapTileIndex.getZoom(pMapTileIndex)
                val x = MapTileIndex.getX(pMapTileIndex)
                val y = MapTileIndex.getY(pMapTileIndex)
                return "${getBaseUrl()}$zoom/$x/$y$mImageFilenameEnding"
            }
        }
    }

    val mapView = remember {
        MapView(context).apply {
            setTileSource(cityMapTileSource)
            setMultiTouchControls(true)
            isTilesScaledToDpi = true
            onResume()
        }
    }

    DisposableEffect(Unit) {
        mapView.onResume()
        onDispose {
            mapView.onPause()
            mapView.onDetach()
        }
    }

    LaunchedEffect(geoPoints) {
        try {
            mapView.overlays.clear()

            if (geoPoints.size >= 2) {
                val polyline = OsmPolyline().apply {
                    outlinePaint.color = AndroidColor.parseColor("#E53935")
                    outlinePaint.strokeWidth = 14f
                    outlinePaint.isAntiAlias = true
                    outlinePaint.strokeCap = android.graphics.Paint.Cap.ROUND
                    outlinePaint.strokeJoin = android.graphics.Paint.Join.ROUND
                    setPoints(geoPoints)
                }
                mapView.overlays.add(polyline)

                // Start Marker (Green)
                val startMarker = OsmMarker(mapView).apply {
                    position = geoPoints.first()
                    title = "Start Point"
                    setAnchor(OsmMarker.ANCHOR_CENTER, OsmMarker.ANCHOR_BOTTOM)
                }
                mapView.overlays.add(startMarker)

                // Finish Marker (Red)
                val finishMarker = OsmMarker(mapView).apply {
                    position = geoPoints.last()
                    title = "Finish Point"
                    setAnchor(OsmMarker.ANCHOR_CENTER, OsmMarker.ANCHOR_BOTTOM)
                }
                mapView.overlays.add(finishMarker)

                // Frame the entire route neatly inside the map box
                val boundingBox = BoundingBox.fromGeoPoints(geoPoints)
                mapView.post {
                    try {
                        mapView.zoomToBoundingBox(boundingBox, true, 80)
                    } catch (_: Exception) {
                        mapView.controller.setCenter(boundingBox.centerWithDateLine)
                        mapView.controller.setZoom(16.0)
                    }
                }
            } else if (geoPoints.isNotEmpty()) {
                val singlePoint = geoPoints.first()
                val marker = OsmMarker(mapView).apply {
                    position = singlePoint
                    title = "Workout Location"
                    setAnchor(OsmMarker.ANCHOR_CENTER, OsmMarker.ANCHOR_BOTTOM)
                }
                mapView.overlays.add(marker)
                mapView.controller.setCenter(singlePoint)
                mapView.controller.setZoom(17.0)
            }

            mapView.invalidate()
        } catch (_: Exception) {}
    }

    AndroidView(
        modifier = modifier,
        factory = { mapView }
    )
}
