package com.simats.hospiq.ui.screens.hospital

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.hospiq.network.models.Doctor
import com.simats.hospiq.network.models.Hospital
import com.simats.hospiq.ui.theme.*
import com.simats.hospiq.utils.DemoData
import com.simats.hospiq.viewmodels.HospitalDetailState
import com.simats.hospiq.viewmodels.HospitalViewModel
import coil.compose.AsyncImage
import com.simats.hospiq.network.ApiConfig
import com.simats.hospiq.ui.components.HospitalsMapDialog

@Composable
fun HospitalDetailScreen(
    hospitalId: Int,
    userLatitude: Double?,
    userLongitude: Double?,
    hospitalViewModel: HospitalViewModel,
    onDoctorClick: (Int) -> Unit,
    onBackClick: () -> Unit
) {
    var showSingleMapDialog by remember { mutableStateOf(false) }
    val detailState by hospitalViewModel.detailState.collectAsState()
    val hospital = when (val s = detailState) {
        is HospitalDetailState.Success -> s.hospital
        else -> DemoData.hospitals.find { it.id == hospitalId } ?: DemoData.hospitals.first()
    }
    val doctors = when (val s = detailState) {
        is HospitalDetailState.Success -> s.doctors
        else -> DemoData.doctors.filter { it.hospitalId == hospitalId }
    }

    LaunchedEffect(hospitalId) { hospitalViewModel.loadHospitalDetail(hospitalId, userLatitude, userLongitude) }

    val facilityIcons = mapOf(
        "ICU" to "🏥", "Emergency" to "🚨", "Pharmacy" to "💊",
        "Lab" to "🔬", "Surgery" to "🔪", "Blood Bank" to "🩸",
        "NICU" to "👶", "Cath Lab" to "❤️"
    )

    Scaffold(
        containerColor = AppBackground,
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceWhite)
                    .padding(16.dp)
            ) {
                Button(
                    onClick = { if (doctors.isNotEmpty()) onDoctorClick(doctors.first().id) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DeepTeal)
                ) {
                    Icon(Icons.Default.DateRange, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Book an appointment", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    ) { innerPadding ->
        if (detailState is HospitalDetailState.Loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = DeepTeal)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
            // Hero image
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .background(DeepTeal)
                ) {
                    if (!hospital.photo.isNullOrEmpty()) {
                        AsyncImage(
                            model = "${ApiConfig.IMAGE_BASE_URL}uploads/hospitals/${hospital.photo}",
                            contentDescription = hospital.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = "🏥",
                            fontSize = 72.sp,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    // Back button
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                            .size(38.dp)
                            .background(Color.White.copy(alpha = 0.9f), CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = DeepTeal)
                    }
                    // Bookmark
                    IconButton(
                        onClick = {},
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .size(38.dp)
                            .background(Color.White.copy(alpha = 0.9f), CircleShape)
                    ) {
                        Icon(Icons.Default.BookmarkBorder, "Save", tint = DeepTeal)
                    }
                }
            }

            // Details
            item {
                Column(modifier = Modifier.background(SurfaceWhite).padding(16.dp)) {
                    Text(hospital.name, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = DeepTeal)
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, null, tint = SlateGray, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(hospital.address, fontSize = 13.sp, color = SlateGray)
                        }
                        TextButton(
                            onClick = { showSingleMapDialog = true },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Icon(Icons.Default.Map, "Map", tint = DeepTeal, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("View on Map", color = DeepTeal, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(hospital.specialties) { spec ->
                            Text(
                                text = spec,
                                fontSize = 12.sp, color = DeepTeal, fontWeight = FontWeight.SemiBold,
                                modifier = Modifier
                                    .background(SoftTeal, RoundedCornerShape(20.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    // Stats row
                    Row(modifier = Modifier.fillMaxWidth()) {
                        StatBox("⭐ ${hospital.avgRating}", "RATINGS", Modifier.weight(1f))
                        VerticalDivider(modifier = Modifier.height(56.dp), color = BorderGray)
                        StatBox("📍 ${hospital.distance}km", "DISTANCE", Modifier.weight(1f))
                        VerticalDivider(modifier = Modifier.height(56.dp), color = BorderGray)
                        StatBox("🕐 Open", hospital.openingHours, Modifier.weight(1f))
                    }
                }
            }

            // About
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("About", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = CharcoalText)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "A premier healthcare facility dedicated to providing compassionate, state-of-the-art medical services. Ranked among the top nationally for patient safety and clinical excellence.",
                        fontSize = 14.sp, color = SlateGray, lineHeight = 22.sp
                    )
                }
            }

            // Facilities
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text("Facilities", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = CharcoalText)
                    Spacer(Modifier.height(12.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(hospital.facilities) { facility ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .background(SoftTeal, RoundedCornerShape(14.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = facilityIcons[facility] ?: "🏥", fontSize = 24.sp)
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(facility, fontSize = 11.sp, color = SlateGray)
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }

            // Doctors
            item {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Doctors", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = CharcoalText, modifier = Modifier.weight(1f))
                    Text("See All", fontSize = 13.sp, color = DeepTeal, fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { })
                }
                Spacer(Modifier.height(8.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(if (doctors.isEmpty()) DemoData.doctors.take(3) else doctors) { doctor ->
                        DoctorMiniCard(doctor = doctor, onBookClick = { onDoctorClick(doctor.id) })
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    if (showSingleMapDialog) {
        val context = androidx.compose.ui.platform.LocalContext.current
        HospitalsMapDialog(
            context = context,
            hospitals = listOf(hospital),
            userLat = userLatitude,
            userLng = userLongitude,
            onNavigateToHospitalDetail = {},
            onDismiss = { showSingleMapDialog = false }
        )
    }
}
}

@Composable
private fun StatBox(value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = CharcoalText)
        Text(label, fontSize = 11.sp, color = SlateGray)
    }
}

@Composable
private fun DoctorMiniCard(doctor: Doctor, onBookClick: () -> Unit) {
    Card(
        modifier = Modifier.width(160.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!doctor.photo.isNullOrEmpty()) {
                AsyncImage(
                    model = "${ApiConfig.IMAGE_BASE_URL}${doctor.photo}",
                    contentDescription = doctor.name,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(IndigoLight, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    val initials = doctor.name.trim().split(" ")
                        .take(2).joinToString("") { it.first().uppercaseChar().toString() }
                    Text(initials, color = IndigoDoctor, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(doctor.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CharcoalText)
            Text(doctor.specialization, fontSize = 12.sp, color = SlateGray)
            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = onBookClick,
                modifier = Modifier.fillMaxWidth().height(34.dp),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DeepTeal),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                Text("Book", color = DeepTeal, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
