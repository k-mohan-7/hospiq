package com.simats.hospiq.ui.screens.doctor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.simats.hospiq.network.ApiConfig
import com.simats.hospiq.navigation.Screen
import com.simats.hospiq.ui.components.*
import com.simats.hospiq.ui.theme.*
import com.simats.hospiq.utils.SessionManager
import com.simats.hospiq.viewmodels.HospitalDetailState
import com.simats.hospiq.viewmodels.HospitalViewModel

@Composable
fun DoctorHospitalScreen(
    sessionManager: SessionManager,
    hospitalViewModel: HospitalViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToAppointments: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToDoctorProfile: (Int) -> Unit = {}
) {
    val hospitalId = sessionManager.getHospitalId()
    val detailState by hospitalViewModel.detailState.collectAsState()
    LaunchedEffect(hospitalId) {
        if (hospitalId != null) hospitalViewModel.loadHospitalDetail(hospitalId)
    }
    val hospital = when (val s = detailState) {
        is HospitalDetailState.Success -> s.hospital
        else -> null
    }
    val doctors = when (val s = detailState) {
        is HospitalDetailState.Success -> s.doctors.take(5)
        else -> emptyList()
    }

    if (hospital == null) {
        // Loading or error state
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (detailState is HospitalDetailState.Loading) {
                CircularProgressIndicator(color = DeepTeal)
            } else {
                Text("Unable to load hospital info", color = SlateGray)
            }
        }
        return
    }

    Scaffold(
        containerColor = AppBackground,
        bottomBar = {
            BottomNavBar(
                currentRoute = Screen.DoctorHospital.route,
                items = doctorNavItems,
                onNavItemClick = { route ->
                    when (route) {
                        Screen.DoctorDashboard.route -> onNavigateToHome()
                        Screen.DoctorAppointments.route -> onNavigateToAppointments()
                        Screen.DoctorNotifications.route -> onNavigateToNotifications()
                        Screen.PatientProfile.route -> onNavigateToProfile()
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {
            // Hero
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(DeepTeal)
                ) {
                    if (!hospital.photo.isNullOrEmpty()) {
                        AsyncImage(
                            model = "${ApiConfig.IMAGE_BASE_URL}uploads/hospitals/${hospital.photo}",
                            contentDescription = hospital.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text("🏥", fontSize = 64.sp, modifier = Modifier.align(Alignment.Center))
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.35f))
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                    ) {
                        Column {
                            Text(hospital.name, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(hospital.city, fontSize = 13.sp, color = Color.White.copy(alpha = 0.85f))
                        }
                    }
                }
            }

            // Stats
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceWhite),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatPill("${doctors.size} Doctors", "Total Doctors", Modifier.weight(1f))
                    StatPill("${hospital.avgRating}⭐", "Rating", Modifier.weight(1f), highlight = true)
                    StatPill("42", "Slots Today", Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))
            }

            // Patient Reviews
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Patient Reviews", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = CharcoalText)
                                Text("${hospital.avgRating}", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = DeepTeal)
                                Text("Overall Rating", fontSize = 13.sp, color = SlateGray)
                                RatingBar(rating = hospital.avgRating)
                            }
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                listOf(5 to 82, 4 to 12, 3 to 4, 2 to 2, 1 to 0).forEach { (star, pct) ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("$star", fontSize = 12.sp, color = SlateGray)
                                        Spacer(Modifier.width(4.dp))
                                        LinearProgressIndicator(
                                            progress = { pct / 100f },
                                            modifier = Modifier.weight(1f).height(6.dp),
                                            color = AmberStar, trackColor = BorderGray
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text("$pct%", fontSize = 11.sp, color = SlateGray)
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        Text("Read all 2,401 reviews →", fontSize = 13.sp, color = DeepTeal, fontWeight = FontWeight.SemiBold)
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            // Expertise
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = IndigoLight)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Expertise", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = CharcoalText)
                        Spacer(Modifier.height(10.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(hospital.specialties) { spec ->
                                Text(
                                    spec, fontSize = 13.sp, color = IndigoDoctor, fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier
                                        .background(Color.White, RoundedCornerShape(20.dp))
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            // Available Doctors
            item {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Available Doctors", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = CharcoalText, modifier = Modifier.weight(1f))
                    Text("View All", fontSize = 13.sp, color = DeepTeal, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(8.dp))
            }

            items(doctors) { doctor ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clickable { onNavigateToDoctorProfile(doctor.id) },
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(1.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        val docPhoto = doctor.photo
                        val docImageUrl = if (!docPhoto.isNullOrEmpty()) {
                            if (docPhoto.startsWith("uploads/")) {
                                "${ApiConfig.IMAGE_BASE_URL}$docPhoto"
                            } else {
                                "${ApiConfig.IMAGE_BASE_URL}uploads/doctors/$docPhoto"
                            }
                        } else null

                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(IndigoLight),
                            contentAlignment = Alignment.Center
                        ) {
                            if (docImageUrl != null) {
                                AsyncImage(
                                    model = docImageUrl,
                                    contentDescription = doctor.name,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                val initials = doctor.name.trim().split(" ")
                                    .take(2).joinToString("") { it.first().uppercaseChar().toString() }
                                Text(initials, color = IndigoDoctor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(doctor.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CharcoalText)
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    if (doctor.rating >= 4.5f) "TOP RATED" else "VERIFIED",
                                    fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MintGreen,
                                    modifier = Modifier.background(MintWash, RoundedCornerShape(4.dp)).padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                            Text("${doctor.specialization} • ${doctor.yearsExperience} yrs", fontSize = 12.sp, color = SlateGray)
                            Text(
                                if (doctor.status == "available") "Available Today" else doctor.status.replace("_", " ").replaceFirstChar { it.uppercaseChar() },
                                fontSize = 12.sp, color = if (doctor.status == "available") MintGreen else AmberStar, fontWeight = FontWeight.SemiBold
                            )
                        }
                        Icon(Icons.Default.ChevronRight, null, tint = DeepTeal)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatPill(value: String, label: String, modifier: Modifier = Modifier, highlight: Boolean = false) {
    Column(
        modifier = modifier
            .background(if (highlight) DeepTeal else SurfaceWhite)
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = if (highlight) Color.White else CharcoalText)
        Text(label, fontSize = 12.sp, color = if (highlight) Color.White.copy(alpha = 0.8f) else SlateGray)
    }
}
