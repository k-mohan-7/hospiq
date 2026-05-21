package com.simats.hospiq.ui.screens.appointments

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.hospiq.navigation.Screen
import com.simats.hospiq.ui.components.*
import com.simats.hospiq.ui.theme.*
import com.simats.hospiq.utils.DemoData
import com.simats.hospiq.utils.SessionManager
import com.simats.hospiq.viewmodels.AppointmentListState
import com.simats.hospiq.viewmodels.AppointmentViewModel

@Composable
fun PatientAppointmentsScreen(
    sessionManager: SessionManager,
    appointmentViewModel: AppointmentViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onAppointmentClick: (Int) -> Unit = {}
) {
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Upcoming", "Completed", "Cancelled")

    val appointmentState by appointmentViewModel.appointmentsState.collectAsState()
    LaunchedEffect(Unit) { appointmentViewModel.loadPatientAppointments(sessionManager.getUserId()) }

    val allAppointments = when (val s = appointmentState) {
        is AppointmentListState.Success -> s.appointments
        else -> DemoData.patientAppointments
    }
    val filtered = when (selectedFilter) {
        "Upcoming" -> allAppointments.filter { it.status in listOf("accepted", "pending") }
        "Completed" -> allAppointments.filter { it.status == "completed" }
        "Cancelled" -> allAppointments.filter { it.status == "cancelled" }
        else -> allAppointments
    }

    Scaffold(
        containerColor = AppBackground,
        bottomBar = {
            BottomNavBar(
                currentRoute = Screen.PatientAppointments.route,
                items = patientNavItems,
                onNavItemClick = { route ->
                    when (route) {
                        Screen.PatientHome.route -> onNavigateToHome()
                        Screen.Search.route -> onNavigateToSearch()
                        Screen.PatientNotifications.route -> onNavigateToNotifications()
                        Screen.PatientProfile.route -> onNavigateToProfile()
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceWhite)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🏥", fontSize = 22.sp)
                Spacer(Modifier.width(8.dp))
                Text("HospiQ", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DeepTeal, modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(IndigoLight, androidx.compose.foundation.shape.CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(sessionManager.getInitials(), color = IndigoDoctor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text("My Appointments", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = CharcoalText)
                Text("Manage your upcoming visits and medical history.", fontSize = 13.sp, color = SlateGray)
            }

            // Filter chips
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filters.forEach { filter ->
                    val isSelected = selectedFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = filter },
                        label = {
                            Text(
                                filter,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        },
                        shape = RoundedCornerShape(20.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DeepTeal,
                            selectedLabelColor = SurfaceWhite
                        )
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            if (filtered.isEmpty()) {
                EmptyState(
                    title = "No appointments",
                    subtitle = "You have no $selectedFilter appointments yet.",
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filtered) { appointment ->
                        val (action1Label, action2Label) = when (appointment.status.lowercase()) {
                            "accepted", "pending" -> "Reschedule" to "Cancel"
                            "completed" -> "View Summary" to ""
                            "cancelled" -> "Rebook Appointment" to ""
                            else -> "" to ""
                        }
                        AppointmentCard(
                            appointment = appointment,
                            isDoctor = false,
                            action1Label = action1Label.takeIf { it.isNotEmpty() },
                            onAction1 = if (action1Label.isNotEmpty()) ({ }) else null,
                            action2Label = action2Label.takeIf { it.isNotEmpty() },
                            onAction2 = if (action2Label.isNotEmpty()) ({ }) else null
                        )
                    }
                }
            }
        }
    }
}
