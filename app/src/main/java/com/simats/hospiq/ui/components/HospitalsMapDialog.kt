package com.simats.hospiq.ui.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.simats.hospiq.network.models.Hospital
import com.simats.hospiq.ui.theme.*
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun HospitalsMapDialog(
    context: Context,
    hospitals: List<Hospital>,
    userLat: Double? = null,
    userLng: Double? = null,
    onNavigateToHospitalDetail: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    // Set user agent for osmdroid
    Configuration.getInstance().userAgentValue = context.packageName

    val initialCenter = if (userLat != null && userLng != null && userLat != 0.0 && userLng != 0.0) {
        GeoPoint(userLat, userLng)
    } else if (hospitals.isNotEmpty() && hospitals.first().latitude != null && hospitals.first().longitude != null) {
        GeoPoint(hospitals.first().latitude!!, hospitals.first().longitude!!)
    } else {
        GeoPoint(13.0827, 80.2707) // Default Chennai
    }

    var selectedHospital by remember { mutableStateOf<Hospital?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize(),
            color = AppBackground
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Interactive OSM Map
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        MapView(ctx).apply {
                            setTileSource(TileSourceFactory.MAPNIK)
                            zoomController.setVisibility(org.osmdroid.views.CustomZoomButtonsController.Visibility.NEVER) // Custom cleaner zoom could be added, but multi-touch is clean
                            setMultiTouchControls(true)
                            controller.setZoom(14.0)
                            controller.setCenter(initialCenter)

                            // Add user location marker
                            if (userLat != null && userLng != null && userLat != 0.0 && userLng != 0.0) {
                                val userMarker = Marker(this)
                                userMarker.position = GeoPoint(userLat, userLng)
                                userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                                userMarker.title = "Your Location"
                                userMarker.icon = ctx.getDrawable(org.osmdroid.library.R.drawable.person)
                                overlays.add(userMarker)
                            }

                            // Add markers for all hospitals
                            hospitals.forEach { hospital ->
                                val lat = hospital.latitude
                                val lng = hospital.longitude
                                if (lat != null && lng != null && lat != 0.0 && lng != 0.0) {
                                    val hospMarker = Marker(this)
                                    hospMarker.position = GeoPoint(lat, lng)
                                    hospMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                    hospMarker.title = hospital.name
                                    hospMarker.subDescription = "${hospital.distance} km away"
                                    
                                    // Custom click listener
                                    hospMarker.setOnMarkerClickListener { m, _ ->
                                        selectedHospital = hospital
                                        controller.animateTo(m.position)
                                        true
                                    }
                                    overlays.add(hospMarker)
                                }
                            }
                        }
                    },
                    update = { _ -> }
                )

                // Top Header Overlay
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter),
                    color = SurfaceWhite.copy(alpha = 0.95f),
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                "Nearby Hospitals Map",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = CharcoalText
                            )
                            Text(
                                "${hospitals.size} hospitals showing",
                                fontSize = 12.sp,
                                color = SlateGray
                            )
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.background(BorderGray.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = CharcoalText)
                        }
                    }
                }

                // Bottom Hospital Details Card Overlay
                val hosp = selectedHospital
                if (hosp != null) {
                    Card(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .navigationBarsPadding()
                            .padding(16.dp)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(8.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = hosp.name,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CharcoalText
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = "${hosp.address}, ${hosp.city}",
                                        fontSize = 12.sp,
                                        color = SlateGray
                                    )
                                }
                                IconButton(
                                    onClick = { selectedHospital = null },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Close, "Clear Selection", tint = SlateGray, modifier = Modifier.size(16.dp))
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.Star, "Rating", tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
                                    Text(
                                        text = hosp.avgRating.toString(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = CharcoalText
                                    )
                                    Text(
                                        text = "(${hosp.totalReviews} reviews)",
                                        fontSize = 12.sp,
                                        color = SlateGray
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .background(SoftTeal, RoundedCornerShape(12.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${hosp.distance} km away",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = DeepTeal
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    onDismiss()
                                    onNavigateToHospitalDetail(hosp.id)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DeepTeal)
                            ) {
                                Text("View Hospital Details", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}
