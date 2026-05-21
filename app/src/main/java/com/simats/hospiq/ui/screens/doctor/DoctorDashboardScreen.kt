package com.simats.hospiq.ui.screens.doctor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
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
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.simats.hospiq.network.ApiConfig
import com.simats.hospiq.navigation.Screen
import com.simats.hospiq.ui.components.*
import com.simats.hospiq.ui.theme.*
import com.simats.hospiq.utils.DemoData
import com.simats.hospiq.utils.SessionManager
import com.simats.hospiq.viewmodels.AppointmentListState
import com.simats.hospiq.viewmodels.AppointmentViewModel
import com.simats.hospiq.viewmodels.DoctorViewModel

@Composable
fun DoctorDashboardScreen(
    sessionManager: SessionManager,
    appointmentViewModel: AppointmentViewModel,
    doctorViewModel: DoctorViewModel,
    onNavigateToHospital: () -> Unit = {},
    onNavigateToAppointments: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val doctorName = sessionManager.getName() ?: "Doctor"
    val doctorFirstName = doctorName.split(" ").firstOrNull() ?: doctorName
    val doctorId = sessionManager.getUserId() // doctor_profiles.id may differ; using user_id for now
    val appointmentState by appointmentViewModel.appointmentsState.collectAsState()
    val appointments = when (val s = appointmentState) {
        is AppointmentListState.Success -> s.appointments
        else -> DemoData.doctorAppointments
    }
    LaunchedEffect(Unit) { appointmentViewModel.loadDoctorAppointments(doctorId) }
    val pending = appointments.filter { it.status == "pending" }
    val confirmed = appointments.filter { it.status == "accepted" }
    val completed = appointments.filter { it.status == "completed" }
    var doctorStatus by remember { mutableStateOf("available") }

    Scaffold(
        containerColor = AppBackground,
        bottomBar = {
            BottomNavBar(
                currentRoute = Screen.DoctorDashboard.route,
                items = doctorNavItems,
                onNavItemClick = { route ->
                    when (route) {
                        Screen.DoctorAppointments.route -> onNavigateToAppointments()
                        Screen.DoctorHospital.route -> onNavigateToHospital()
                        Screen.DoctorNotifications.route -> onNavigateToNotifications()
                        Screen.PatientProfile.route -> onNavigateToProfile()
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {
            // Top bar
            item {
                Surface(shadowElevation = 2.dp) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceWhite)
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.res.painterResource(id = com.simats.hospiq.R.drawable.app_logo),
                            contentDescription = "Logo",
                            modifier = Modifier.size(34.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text("HospiQ", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DeepTeal, modifier = Modifier.weight(1f))
                        
                        val profilePhotoPath = sessionManager.getProfilePhoto()
                        val imageUrl = if (!profilePhotoPath.isNullOrEmpty()) {
                            "${ApiConfig.IMAGE_BASE_URL}$profilePhotoPath"
                        } else null

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(IndigoDoctor)
                                .clickable { onNavigateToProfile() },
                            contentAlignment = Alignment.Center
                        ) {
                            if (imageUrl != null) {
                                AsyncImage(
                                    model = imageUrl,
                                    contentDescription = "Profile Photo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(sessionManager.getInitials(), color = SurfaceWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // Greeting
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
                    Text("Good morning, $doctorFirstName 👋",
                        fontSize = 20.sp, fontWeight = FontWeight.Bold, color = CharcoalText)
                    Text("Schedule for today", fontSize = 14.sp, color = SlateGray)
                }
            }

            // Status toggle card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(2.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Your Current Status", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = CharcoalText)
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            StatusToggle("✅ Available", "available", doctorStatus) { newStatus ->
                                doctorStatus = newStatus
                                doctorViewModel.updateStatus(doctorId, newStatus)
                            }
                            StatusToggle("🕐 Busy", "busy", doctorStatus) { newStatus ->
                                doctorStatus = newStatus
                                doctorViewModel.updateStatus(doctorId, newStatus)
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        StatusToggle("🔪 In Surgery", "in_surgery", doctorStatus, full = true) { newStatus ->
                            doctorStatus = newStatus
                            doctorViewModel.updateStatus(doctorId, newStatus)
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            // Stats card (teal)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DeepTeal)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("${appointments.size}", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Total Patients", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
                                Text("TODAY'S LOAD", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f), fontWeight = FontWeight.SemiBold)
                            }
                            Icon(Icons.AutoMirrored.Filled.TrendingUp, null, tint = MintGreen, modifier = Modifier.size(40.dp))
                        }
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            MiniStat("Pending", "${pending.size}", Modifier.weight(1f))
                            MiniStat("Confirmed", "${confirmed.size}", Modifier.weight(1f))
                            MiniStat("Completed", "${completed.size}", Modifier.weight(1f))
                            MiniStat("Wait", "15m", Modifier.weight(1f))
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            // Today's appointments header
            item {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Today's appointments", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = CharcoalText, modifier = Modifier.weight(1f))
                    TextButton(onClick = onNavigateToAppointments) {
                        Text("View All →", color = DeepTeal, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }
            }

            // Pending appointments
            items(pending.take(2)) { appt ->
                AppointmentCard(
                    appointment = appt,
                    isDoctor = true,
                    action1Label = "Accept",
                    onAction1 = {},
                    action2Label = "Reject",
                    onAction2 = {},
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            // Confirmed appointments
            items(confirmed.take(2)) { appt ->
                AppointmentCard(
                    appointment = appt,
                    isDoctor = true,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            // Sentiment card
            item {
                Spacer(Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = IndigoLight)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("📊", fontSize = 28.sp)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Patient Sentiment", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = IndigoDoctor)
                            Text("Overall satisfaction is up 12% this week", fontSize = 13.sp, color = SlateGray)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusToggle(
    label: String, value: String, selected: String,
    full: Boolean = false, onSelect: (String) -> Unit
) {
    val isSelected = selected == value
    Box(
        modifier = Modifier
            .then(if (full) Modifier.fillMaxWidth() else Modifier)
            .background(
                if (isSelected) SoftTeal else SurfaceWhite,
                RoundedCornerShape(12.dp)
            )
            .border(1.5.dp, if (isSelected) DeepTeal else BorderGray, RoundedCornerShape(12.dp))
            .clickable { onSelect(value) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, fontSize = 14.sp, color = if (isSelected) DeepTeal else SlateGray, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal)
    }
}

@Composable
private fun MiniStat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(10.dp)).padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(label, fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
    }
}
