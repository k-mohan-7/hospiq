package com.simats.hospiq.ui.screens.appointments

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import com.simats.hospiq.navigation.Screen
import com.simats.hospiq.ui.components.*
import com.simats.hospiq.ui.theme.*
import com.simats.hospiq.network.models.Appointment
import com.simats.hospiq.utils.SessionManager
import com.simats.hospiq.viewmodels.AppointmentListState
import com.simats.hospiq.viewmodels.AppointmentViewModel
import androidx.compose.ui.platform.LocalContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import android.app.DatePickerDialog
import java.util.Calendar

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

    var reschedulingAppointment by remember { mutableStateOf<Appointment?>(null) }

    val appointmentState by appointmentViewModel.appointmentsState.collectAsState()
    LaunchedEffect(Unit) { appointmentViewModel.loadPatientAppointments(sessionManager.getUserId()) }

    val allAppointments = when (val s = appointmentState) {
        is AppointmentListState.Success -> s.appointments
        else -> emptyList()
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
        if (appointmentState is AppointmentListState.Loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = DeepTeal)
            }
        } else {
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
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = com.simats.hospiq.R.drawable.app_logo),
                    contentDescription = "Logo",
                    modifier = Modifier.size(34.dp)
                )
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
                            "accepted", "pending", "rescheduled" -> "Reschedule" to "Cancel"
                            "completed" -> "View Summary" to ""
                            "cancelled" -> "Rebook Appointment" to ""
                            else -> "" to ""
                        }
                        AppointmentCard(
                            appointment = appointment,
                            isDoctor = false,
                            action1Label = action1Label.takeIf { it.isNotEmpty() },
                            onAction1 = if (action1Label.isNotEmpty()) ({
                                if (action1Label == "Reschedule") {
                                    reschedulingAppointment = appointment
                                }
                            }) else null,
                            action2Label = action2Label.takeIf { it.isNotEmpty() },
                            onAction2 = if (action2Label.isNotEmpty()) ({
                                if (action2Label == "Cancel") {
                                    appointmentViewModel.cancelAppointment(appointment.id) {
                                        appointmentViewModel.loadPatientAppointments(sessionManager.getUserId())
                                    }
                                }
                            }) else null
                        )
                    }
                }
            }

            reschedulingAppointment?.let { appt ->
                RescheduleDialog(
                    appointment = appt,
                    onDismiss = { reschedulingAppointment = null },
                    onConfirm = { newDate, newTime ->
                        appointmentViewModel.rescheduleAppointment(appt.id, newDate, newTime) {
                            appointmentViewModel.loadPatientAppointments(sessionManager.getUserId())
                            reschedulingAppointment = null
                        }
                    }
                )
            }
        }
    }
}
}

@Composable
fun RescheduleDialog(
    appointment: Appointment,
    onDismiss: () -> Unit,
    onConfirm: (newDate: String, newTime: String) -> Unit
) {
    val context = LocalContext.current
    val today = remember { LocalDate.now() }
    val dynamicDates = remember {
        (0..5).map { offset ->
            today.plusDays(offset.toLong())
        }
    }
    val formattedDates = dynamicDates.map { date ->
        date.format(DateTimeFormatter.ISO_DATE)
    }

    var selectedDateString by remember { mutableStateOf(appointment.date) }
    var selectedSlotTime by remember { mutableStateOf<String?>(null) }

    fun showCustomDatePicker() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val chosenDate = LocalDate.of(year, month + 1, dayOfMonth)
                val chosenDateStr = chosenDate.format(DateTimeFormatter.ISO_DATE)
                selectedDateString = chosenDateStr
                selectedSlotTime = null
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    val filteredSlots = emptyList<com.simats.hospiq.network.models.TimeSlot>()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Reschedule Appointment",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = CharcoalText
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Pick a Date",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = CharcoalText
                )

                // Date Selector row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val parsedDate = try {
                        LocalDate.parse(selectedDateString).format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
                    } catch (_: Exception) {
                        selectedDateString
                    }
                    Text(parsedDate, fontSize = 13.sp, color = DeepTeal, fontWeight = FontWeight.SemiBold)
                    
                    IconButton(onClick = { showCustomDatePicker() }) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Choose Custom Date",
                            tint = DeepTeal
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                // Time Slot selector
                Text(
                    text = "Select Time Slot",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = CharcoalText
                )

                // Grid of slots
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 200.dp).verticalScroll(rememberScrollState())
                ) {
                    for (row in 0 until (filteredSlots.size + 2) / 3) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            for (col in 0 until 3) {
                                val index = row * 3 + col
                                if (index < filteredSlots.size) {
                                    val slot = filteredSlots[index]
                                    val isSelected = selectedSlotTime == slot.slotTime
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(
                                                if (isSelected) DeepTeal else SurfaceWhite,
                                                RoundedCornerShape(12.dp)
                                            )
                                            .border(
                                                1.dp,
                                                if (isSelected) DeepTeal else BorderGray,
                                                RoundedCornerShape(12.dp)
                                            )
                                            .clickable { selectedSlotTime = slot.slotTime }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = slot.slotTime,
                                            fontSize = 12.sp,
                                            color = if (isSelected) Color.White else CharcoalText
                                        )
                                    }
                                } else {
                                    Spacer(Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedSlotTime?.let { time ->
                        onConfirm(selectedDateString, time)
                    }
                },
                enabled = selectedSlotTime != null,
                colors = ButtonDefaults.buttonColors(containerColor = DeepTeal)
            ) {
                Text("Confirm", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = SlateGray)
            }
        }
    )
}
