package com.simats.hospiq.ui.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun OSMMapPicker(
    context: Context,
    initialLat: Double = 13.0827, // Chennai coordinates as default
    initialLng: Double = 80.2707,
    onLocationSelected: (Double, Double) -> Unit,
    onDismiss: () -> Unit
) {
    // Initialize OSM Configuration to avoid user-agent blocks
    Configuration.getInstance().userAgentValue = context.packageName

    var selectedLocation by remember { mutableStateOf(GeoPoint(initialLat, initialLng)) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f)) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { ctx ->
                            MapView(ctx).apply {
                                setTileSource(TileSourceFactory.MAPNIK)
                                zoomController.setVisibility(org.osmdroid.views.CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT)
                                setMultiTouchControls(true)
                                controller.setZoom(15.0)
                                controller.setCenter(selectedLocation)

                                // Add draggable marker
                                val marker = Marker(this)
                                marker.position = selectedLocation
                                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                marker.title = "Tap anywhere or drag to place"
                                marker.isDraggable = true
                                marker.setOnMarkerDragListener(object : Marker.OnMarkerDragListener {
                                    override fun onMarkerDrag(m: Marker?) {}
                                    override fun onMarkerDragEnd(m: Marker?) {
                                        m?.position?.let {
                                            selectedLocation = it
                                        }
                                    }
                                    override fun onMarkerDragStart(m: Marker?) {}
                                })
                                overlays.add(marker)

                                // Single tap to place marker
                                val mapEventsOverlay = org.osmdroid.views.overlay.MapEventsOverlay(
                                    object : org.osmdroid.events.MapEventsReceiver {
                                        override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                                            p?.let {
                                                selectedLocation = it
                                                marker.position = it
                                                invalidate()
                                            }
                                            return true
                                        }

                                        override fun longPressHelper(p: GeoPoint?): Boolean {
                                            return false
                                        }
                                    }
                                )
                                overlays.add(mapEventsOverlay)
                            }
                        },
                        update = { mapView ->
                            // Update map if needed
                        }
                    )

                    // Coordinate Card Overlay
                    Card(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f))
                    ) {
                        Text(
                            text = "Lat: ${"%.5f".format(selectedLocation.latitude)} | Lng: ${"%.5f".format(selectedLocation.longitude)}",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = MaterialTheme.colorScheme.error)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onLocationSelected(selectedLocation.latitude, selectedLocation.longitude)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00796B))
                    ) {
                        Text("Select Location", color = Color.White)
                    }
                }
            }
        }
    }
}
