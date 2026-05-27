package com.simats.hospiq.ui.screens.doctor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.hospiq.navigation.Screen
import com.simats.hospiq.ui.components.*
import com.simats.hospiq.ui.theme.*
import com.simats.hospiq.network.models.Appointment
import com.simats.hospiq.utils.SessionManager
import com.simats.hospiq.viewmodels.AppointmentListState
import com.simats.hospiq.viewmodels.AppointmentViewModel

@Composable
fun DoctorAppointmentsScreen(
    sessionManager: SessionManager,
    appointmentViewModel: AppointmentViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToHospital: () -> Unit = {}
) {
    val doctorId = sessionManager.getDoctorId() ?: sessionManager.getUserId()
    val context = androidx.compose.ui.platform.LocalContext.current
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Pending", "Confirmed", "Past")
    val apptState by appointmentViewModel.appointmentsState.collectAsState()
    LaunchedEffect(Unit) { appointmentViewModel.loadDoctorAppointments(doctorId) }
    val allAppointments = when (val s = apptState) {
        is AppointmentListState.Success -> s.appointments
        else -> emptyList()
    }
    val filtered = when (selectedFilter) {
        "Pending" -> allAppointments.filter { it.status == "pending" }
        "Confirmed" -> allAppointments.filter { it.status == "accepted" }
        "Past" -> allAppointments.filter { it.status in listOf("completed", "rejected") }
        else -> allAppointments
    }
    val pendingList = allAppointments.filter { it.status == "pending" }
    val confirmedList = allAppointments.filter { it.status == "accepted" }
    val pastList = allAppointments.filter { it.status in listOf("completed", "rejected", "cancelled") }
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
                currentRoute = Screen.DoctorAppointments.route,
                items = doctorNavItems,
                onNavItemClick = { route ->
                    when (route) {
                        Screen.DoctorDashboard.route -> onNavigateToHome()
                        Screen.DoctorHospital.route -> onNavigateToHospital()
                        Screen.DoctorNotifications.route -> onNavigateToNotifications()
                        Screen.PatientProfile.route -> onNavigateToProfile()
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                // Top bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceWhite)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Appointments",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalText,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Filter row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filters.forEach { filter ->
                        val isSelected = selectedFilter == filter
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isSelected) DeepTeal else SurfaceWhite,
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable { selectedFilter = filter }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                filter,
                                color = if (isSelected) SurfaceWhite else SlateGray,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // Appointment list
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (pendingList.isNotEmpty() && (selectedFilter == "All" || selectedFilter == "Pending")) {
                        item {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("New Requests", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = CharcoalText, modifier = Modifier.weight(1f))
                                Box(
                                    modifier = Modifier
                                        .background(CoralWash, RoundedCornerShape(20.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("${pendingList.size} PENDING", fontSize = 11.sp, color = CoralOrange, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        items(pendingList) { appt ->
                            AppointmentCard(
                                appointment = appt, isDoctor = true,
                                action1Label = "Accept", onAction1 = {
                                    appointmentViewModel.acceptAppointment(context, appt.id) {
                                        appointmentViewModel.loadDoctorAppointments(doctorId)
                                    }
                                },
                                action2Label = "Reject", onAction2 = {
                                    appointmentViewModel.rejectAppointment(context, appt.id) {
                                        appointmentViewModel.loadDoctorAppointments(doctorId)
                                    }
                                },
                                onCardClick = {
                                    selectedAppointmentForDetail = appt
                                }
                            )
                        }
                    }

                    if (confirmedList.isNotEmpty() && (selectedFilter == "All" || selectedFilter == "Confirmed")) {
                        item {
                            Spacer(Modifier.height(4.dp))
                            Text("Upcoming Today", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = CharcoalText)
                        }
                        items(confirmedList) { appt ->
                            AppointmentCard(
                                appointment = appt, isDoctor = true,
                                action1Label = "Mark Complete", onAction1 = {
                                    selectedAppointmentForDetail = appt
                                },
                                action2Label = "Cancel", onAction2 = {
                                    appointmentViewModel.cancelAppointment(appt.id) {
                                        appointmentViewModel.loadDoctorAppointments(doctorId)
                                    }
                                },
                                onCardClick = {
                                    selectedAppointmentForDetail = appt
                                }
                            )
                        }
                    }

                    if (pastList.isNotEmpty() && (selectedFilter == "All" || selectedFilter == "Past")) {
                        item {
                            Spacer(Modifier.height(4.dp))
                            Text("Past Appointments", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = CharcoalText)
                        }
                        items(pastList) { appt ->
                            AppointmentCard(
                                appointment = appt, isDoctor = true,
                                onCardClick = {
                                    selectedAppointmentForDetail = appt
                                },
                                modifier = Modifier.alpha(0.6f)
                            )
                        }
                    }

                    if (filtered.isEmpty()) {
                        item { EmptyState(title = "No appointments", subtitle = "No $selectedFilter appointments found.") }
                    }
                }
            }

            val appt = selectedAppointmentForDetail
            if (appt != null) {
                PatientDetailsAdviceDialog(
                    appointment = appt,
                    allAppointments = allAppointments,
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
