package com.simats.hospiq.ui.screens.doctor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.draw.alpha
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
import com.simats.hospiq.network.models.Appointment

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
    val doctorId = sessionManager.getDoctorId() ?: sessionManager.getUserId()
    val context = androidx.compose.ui.platform.LocalContext.current
    val appointmentsState by appointmentViewModel.appointmentsState.collectAsState()
    val appointments = when (val s = appointmentsState) {
        is AppointmentListState.Success -> s.appointments
        else -> emptyList()
    }
    val doctorStatus by doctorViewModel.doctorStatus.collectAsState()
    val doctorPatients by doctorViewModel.doctorPatients.collectAsState()

    LaunchedEffect(doctorId) {
        appointmentViewModel.loadDoctorAppointments(doctorId)
        doctorViewModel.loadDoctorProfile(doctorId)
        doctorViewModel.loadDoctorPatients(doctorId)
    }
    val pending = appointments.filter { it.status == "pending" }
    val confirmed = appointments.filter { it.status == "accepted" }
    val completed = appointments.filter { it.status == "completed" }
    var selectedAppointmentForDetail by remember { mutableStateOf<Appointment?>(null) }
    val healthReports by appointmentViewModel.healthReports.collectAsState()
    LaunchedEffect(selectedAppointmentForDetail) {
        selectedAppointmentForDetail?.let {
            appointmentViewModel.loadPatientHealthReports(it.patientId)
        }
    }

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
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(bottom = 80.dp)
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
                                    doctorViewModel.updateStatus(context, doctorId, newStatus)
                                }
                                StatusToggle("🕐 Busy", "busy", doctorStatus) { newStatus ->
                                    doctorViewModel.updateStatus(context, doctorId, newStatus)
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                            StatusToggle("🔪 In Surgery", "in_surgery", doctorStatus, full = true) { newStatus ->
                                doctorViewModel.updateStatus(context, doctorId, newStatus)
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
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    MiniStat("PENDING", "${pending.size}")
                                    MiniStat("CONFIRMED", "${confirmed.size}")
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                }

                // ── My Patients Section ─────────────────────────────
                if (doctorPatients.isNotEmpty()) {
                    item {
                        Text(
                            text = "My Patients",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = CharcoalText,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(doctorPatients) { patient ->
                                Card(
                                    modifier = Modifier
                                        .width(180.dp)
                                        .clickable {
                                            val latestAppt = appointments.find { it.patientId == patient.patientId } 
                                                ?: Appointment(
                                                    patientId = patient.patientId,
                                                    patientName = patient.fullName,
                                                    doctorId = doctorId
                                                )
                                            selectedAppointmentForDetail = latestAppt
                                        },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(50.dp)
                                                .clip(CircleShape)
                                                .background(SoftTeal),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (!patient.profilePhoto.isNullOrEmpty()) {
                                                AsyncImage(
                                                    model = "${ApiConfig.IMAGE_BASE_URL}uploads/profile/${patient.profilePhoto}",
                                                    contentDescription = patient.fullName,
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                            } else {
                                                Text(
                                                    text = patient.fullName.take(1).uppercase(),
                                                    color = DeepTeal,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 18.sp
                                                )
                                            }
                                        }
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            text = patient.fullName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = CharcoalText,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = patient.lastIllnessName ?: "General Care",
                                            fontSize = 11.sp,
                                            color = SlateGray,
                                            maxLines = 1
                                        )
                                        Spacer(Modifier.height(6.dp))
                                        Text(
                                            text = "${patient.totalAppointments} appts",
                                            fontSize = 11.sp,
                                            color = DeepTeal,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                }

                // Header section
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Today's appointments", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = CharcoalText, modifier = Modifier.weight(1f))
                        TextButton(onClick = onNavigateToAppointments) {
                            Text("View All →", color = DeepTeal, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }
                }

                items(pending.take(2)) { appt ->
                    AppointmentCard(
                        appointment = appt,
                        isDoctor = true,
                        action1Label = "Accept",
                        onAction1 = {
                            appointmentViewModel.acceptAppointment(context, appt.id) {
                                appointmentViewModel.loadDoctorAppointments(doctorId)
                            }
                        },
                        action2Label = "Reject",
                        onAction2 = {
                            appointmentViewModel.rejectAppointment(context, appt.id) {
                                appointmentViewModel.loadDoctorAppointments(doctorId)
                            }
                        },
                        onCardClick = {
                            selectedAppointmentForDetail = appt
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }

                // Confirmed appointments
                items(confirmed.take(2)) { appt ->
                    AppointmentCard(
                        appointment = appt,
                        isDoctor = true,
                        onCardClick = {
                            selectedAppointmentForDetail = appt
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }

                // Completed appointments
                val cancelled = appointments.filter { it.status == "cancelled" || it.status == "rejected" }
                if (completed.isNotEmpty() || cancelled.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(16.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Completed / Cancelled", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = CharcoalText)
                        }
                    }

                    items(completed.take(2)) { appt ->
                        AppointmentCard(
                            appointment = appt,
                            isDoctor = true,
                            onCardClick = {
                                selectedAppointmentForDetail = appt
                            },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp).alpha(0.7f)
                        )
                    }

                    items(cancelled.take(2)) { appt ->
                        AppointmentCard(
                            appointment = appt,
                            isDoctor = true,
                            onCardClick = {
                                selectedAppointmentForDetail = appt
                            },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp).alpha(0.5f)
                        )
                    }
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

            val appt = selectedAppointmentForDetail
            if (appt != null) {
                PatientDetailsAdviceDialog(
                    appointment = appt,
                    allAppointments = appointments,
                    healthReports = healthReports,
                    onDismiss = { selectedAppointmentForDetail = null },
                    onSubmitReport = { status, notes, docs, docNames ->
                        appointmentViewModel.submitHealthReport(
                            appointmentId = appt.id,
                            patientId = appt.patientId,
                            doctorId = appt.doctorId,
                            healthStatus = status,
                            notes = notes,
                            documentBytesList = docs,
                            documentNames = docNames
                        ) {
                            selectedAppointmentForDetail = null
                            appointmentViewModel.loadDoctorAppointments(doctorId)
                        }
                    },
                    onEditReport = { reportId, status, notes ->
                        appointmentViewModel.editHealthReport(reportId, appt.patientId, status, notes)
                    },
                    onDeleteReport = { reportId ->
                        appointmentViewModel.deleteHealthReport(reportId, appt.patientId)
                    }
                )
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
