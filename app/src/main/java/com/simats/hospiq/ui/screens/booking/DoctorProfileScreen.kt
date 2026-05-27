package com.simats.hospiq.ui.screens.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.hospiq.network.models.TimeSlot
import com.simats.hospiq.ui.theme.*
import com.simats.hospiq.utils.DemoData
import com.simats.hospiq.utils.SessionManager
import com.simats.hospiq.viewmodels.AppointmentViewModel
import com.simats.hospiq.viewmodels.BookingState
import com.simats.hospiq.viewmodels.DoctorProfileState
import com.simats.hospiq.viewmodels.DoctorViewModel
import coil.compose.AsyncImage
import com.simats.hospiq.network.ApiConfig
import androidx.compose.ui.platform.LocalContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import android.app.DatePickerDialog
import java.util.Calendar

@Composable
fun DoctorProfileScreen(
    doctorId: Int,
    doctorViewModel: DoctorViewModel,
    appointmentViewModel: AppointmentViewModel,
    sessionManager: SessionManager,
    onBackClick: () -> Unit,
    onConfirmAppointment: (doctorId: Int, slotId: Int, appointmentId: Int) -> Unit
) {
    val profileState by doctorViewModel.profileState.collectAsState()
    val bookingState by appointmentViewModel.bookingState.collectAsState()
    val doctor = when (val s = profileState) {
        is DoctorProfileState.Success -> s.doctor
        else -> DemoData.doctors.find { it.id == doctorId } ?: DemoData.doctors.first()
    }
    val slots = when (val s = profileState) {
        is DoctorProfileState.Success -> s.slots
        else -> DemoData.timeSlots
    }

    LaunchedEffect(doctorId) { doctorViewModel.loadDoctorProfile(doctorId) }

    // Navigate on successful booking
    LaunchedEffect(bookingState) {
        if (bookingState is BookingState.Success) {
            val apptId = (bookingState as BookingState.Success).appointmentId
            onConfirmAppointment(doctorId, 0, apptId)
            appointmentViewModel.resetBooking()
        }
    }

    val today = remember { LocalDate.now() }
    val dynamicDates = remember {
        (0..5).map { offset ->
            today.plusDays(offset.toLong())
        }
    }
    val dayLabels = dynamicDates.map { date ->
        date.dayOfWeek.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
    }
    val dateLabels = dynamicDates.map { date ->
        date.dayOfMonth
    }
    val formattedDates = dynamicDates.map { date ->
        date.format(DateTimeFormatter.ISO_DATE)
    }

    var selectedDateString by remember { mutableStateOf(formattedDates.firstOrNull() ?: "2026-05-22") }
    var selectedSlotId by remember { mutableStateOf<Int?>(null) }
    var consultationType by remember { mutableStateOf("in_person") }
    
    var showAvailabilitySettings by remember { mutableStateOf(false) }
    val isOwnProfile = sessionManager.getRole() == "doctor" && sessionManager.getDoctorId() == doctorId

    val context = LocalContext.current
    fun showCustomDatePicker() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val chosenDate = LocalDate.of(year, month + 1, dayOfMonth)
                val chosenDateStr = chosenDate.format(DateTimeFormatter.ISO_DATE)
                selectedDateString = chosenDateStr
                selectedSlotId = null
                doctorViewModel.loadSlotsForDate(doctorId, chosenDateStr)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
        datePickerDialog.show()
    }

    val selectedSlot = slots.find { it.id == selectedSlotId }
    
    // RESTRICT slots using doctor's availability slots if enabled
    val filteredSlots = if (DemoData.dynamicTimingsEnabled) {
        slots.filter { DemoData.activeSlots.contains(it.id) }
    } else {
        slots
    }
    val morningSlots = filteredSlots.filter { it.id <= 6 }
    val afternoonSlots = filteredSlots.filter { it.id > 6 }

    Scaffold(
        containerColor = AppBackground,
        topBar = {
            Surface(shadowElevation = 2.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceWhite)
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = CharcoalText)
                    }
                    Text(
                        "Doctor Profile", fontSize = 18.sp, fontWeight = FontWeight.Bold,
                        color = CharcoalText, modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Share, "Share", tint = SlateGray)
                    }
                }
            }
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Column(modifier = Modifier.background(SurfaceWhite).padding(16.dp)) {
                    if (isOwnProfile) {
                        Button(
                            onClick = { showAvailabilitySettings = true },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = IndigoDoctor)
                        ) {
                            Icon(Icons.Default.DateRange, null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Create Slots", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    } else {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Selected Slot", fontSize = 12.sp, color = SlateGray)
                                Text(
                                    text = selectedSlot?.slotTime ?: "None selected",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedSlot != null) DeepTeal else CoralOrange
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = {
                                selectedSlotId?.let { slotId ->
                                    appointmentViewModel.bookAppointment(
                                        patientId = sessionManager.getUserId(),
                                        doctorId = doctorId,
                                        slotId = slotId,
                                        consultationType = consultationType,
                                        date = selectedDateString
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedSlot != null) DeepTeal else DisabledGray
                            ),
                            enabled = selectedSlot != null && bookingState !is BookingState.Loading
                        ) {
                            if (bookingState is BookingState.Loading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                            } else {
                                Icon(Icons.Default.DateRange, null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Confirm Appointment", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        if (showAvailabilitySettings) {
            com.simats.hospiq.ui.screens.profile.CustomSlotCreatorDialog(
                doctorViewModel = doctorViewModel,
                doctorId = doctorId,
                onDismiss = {
                    showAvailabilitySettings = false
                    doctorViewModel.loadDoctorProfile(doctorId)
                }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Doctor header card
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar
                    if (!doctor.photo.isNullOrEmpty()) {
                        AsyncImage(
                            model = "${ApiConfig.IMAGE_BASE_URL}${doctor.photo}",
                            contentDescription = doctor.name,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(IndigoLight, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            val initials = doctor.name.trim().split(" ")
                                .take(2).joinToString("") { it.first().uppercaseChar().toString() }
                            Text(initials, color = IndigoDoctor, fontWeight = FontWeight.Bold, fontSize = 28.sp)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(doctor.name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = CharcoalText)
                    Text(
                        "${doctor.specialization} • ${doctor.hospitalName}",
                        fontSize = 13.sp, color = SlateGray
                    )
                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = BorderGray)
                    Spacer(Modifier.height(14.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        DoctorStat("⭐ ${doctor.rating}", "Rating", Modifier.weight(1f))
                        VerticalDivider(modifier = Modifier.height(40.dp), color = BorderGray)
                        DoctorStat("${doctor.yearsExperience}+ yrs", "Exp", Modifier.weight(1f))
                        VerticalDivider(modifier = Modifier.height(40.dp), color = BorderGray)
                        DoctorStat("${doctor.totalPatients / 1000}.${(doctor.totalPatients % 1000) / 100}k", "Patients", Modifier.weight(1f))
                    }
                }
            }

            // Date selector
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Select Date", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = CharcoalText)
                            val parsedDate = try {
                                LocalDate.parse(selectedDateString).format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
                            } catch (_: Exception) {
                                selectedDateString
                            }
                            Text(parsedDate, fontSize = 13.sp, color = DeepTeal, fontWeight = FontWeight.SemiBold)
                        }
                        IconButton(onClick = { showCustomDatePicker() }) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "Choose Custom Date",
                                tint = DeepTeal
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        itemsIndexed(formattedDates) { index, dateStr ->
                            val isSelected = selectedDateString == dateStr
                            Column(
                                modifier = Modifier
                                    .size(52.dp)
                                    .background(
                                        if (isSelected) DeepTeal else SurfaceWhite,
                                        RoundedCornerShape(14.dp)
                                    )
                                    .border(1.dp, if (isSelected) DeepTeal else BorderGray, RoundedCornerShape(14.dp))
                                    .clickable { 
                                        selectedDateString = dateStr
                                        selectedSlotId = null
                                        doctorViewModel.loadSlotsForDate(doctorId, dateStr)
                                    },
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(dayLabels[index], fontSize = 11.sp, color = if (isSelected) Color.White else SlateGray)
                                Text(
                                    "${dateLabels[index]}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else CharcoalText
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    // Slots
                    Text("Available Slots", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = CharcoalText)
                    Spacer(Modifier.height(8.dp))
                    Text("MORNING", fontSize = 11.sp, color = SlateGray, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    SlotGrid(slots = morningSlots, selectedSlotId = selectedSlotId, onSlotClick = { selectedSlotId = it })
                    Spacer(Modifier.height(12.dp))
                    Text("AFTERNOON", fontSize = 11.sp, color = SlateGray, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    SlotGrid(slots = afternoonSlots, selectedSlotId = selectedSlotId, onSlotClick = { selectedSlotId = it })
                }
            }

            Spacer(Modifier.height(16.dp))

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun DoctorStat(value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = CharcoalText)
        Text(label, fontSize = 12.sp, color = SlateGray)
    }
}

@Composable
private fun ConsultTypeChip(
    label: String, value: String, selected: String, onSelect: (String) -> Unit
) {
    val isSelected = selected == value
    Box(
        modifier = Modifier
            .background(if (isSelected) DeepTeal else SurfaceWhite, RoundedCornerShape(20.dp))
            .border(1.dp, if (isSelected) DeepTeal else BorderGray, RoundedCornerShape(20.dp))
            .clickable { onSelect(value) }
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(label, fontSize = 14.sp, color = if (isSelected) Color.White else CharcoalText, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun SlotGrid(slots: List<TimeSlot>, selectedSlotId: Int?, onSlotClick: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (row in 0 until (slots.size + 2) / 3) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (col in 0 until 3) {
                    val index = row * 3 + col
                    if (index < slots.size) {
                        val slot = slots[index]
                        val isSelected = selectedSlotId == slot.id
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    when {
                                        slot.isBooked -> DisabledGray.copy(alpha = 0.3f)
                                        isSelected -> DeepTeal
                                        else -> SurfaceWhite
                                    },
                                    RoundedCornerShape(12.dp)
                                )
                                .border(
                                    1.dp,
                                    when {
                                        slot.isBooked -> DisabledGray
                                        isSelected -> DeepTeal
                                        else -> BorderGray
                                    },
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable(enabled = !slot.isBooked) { onSlotClick(slot.id) }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = slot.slotTime,
                                fontSize = 13.sp,
                                color = when {
                                    slot.isBooked -> SlateGray
                                    isSelected -> Color.White
                                    else -> CharcoalText
                                },
                                textDecoration = if (slot.isBooked) TextDecoration.LineThrough else null
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
