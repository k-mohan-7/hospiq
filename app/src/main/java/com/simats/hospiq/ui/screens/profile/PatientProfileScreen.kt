package com.simats.hospiq.ui.screens.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import java.util.Calendar
import java.util.Locale
import java.text.SimpleDateFormat
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.simats.hospiq.navigation.Screen
import com.simats.hospiq.ui.components.*
import com.simats.hospiq.ui.theme.*
import com.simats.hospiq.utils.SessionManager
import com.simats.hospiq.utils.DemoData
import com.simats.hospiq.viewmodels.DoctorProfileState
import com.simats.hospiq.viewmodels.AuthViewModel
import com.simats.hospiq.viewmodels.AppointmentViewModel
import com.simats.hospiq.viewmodels.DoctorViewModel
import com.simats.hospiq.viewmodels.AppointmentListState
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi

@Composable
fun PatientProfileScreen(
    sessionManager: SessionManager,
    authViewModel: AuthViewModel,
    appointmentViewModel: AppointmentViewModel,
    doctorViewModel: DoctorViewModel,
    onLogout: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToAppointments: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToSettings: () -> Unit = {}
) {
    val context = LocalContext.current
    val isDoctor = sessionManager.getRole() == "doctor"
    val initials = sessionManager.getInitials()
    val userName = sessionManager.getName() ?: "Guest"
    val subtitle = if (isDoctor) "Specialist Account" else "Patient Account"

    // Load appointments for dynamic counts
    val userId = sessionManager.getUserId()
    val doctorId = sessionManager.getDoctorId() ?: userId
    LaunchedEffect(userId) {
        if (userId != -1) {
            if (isDoctor) {
                appointmentViewModel.loadDoctorAppointments(doctorId)
                doctorViewModel.loadDoctorProfile(doctorId) // Load real slots/status from DB
            } else {
                appointmentViewModel.loadPatientAppointments(userId)
            }
        }
    }

    val appointmentsState by appointmentViewModel.appointmentsState.collectAsState()
    val apptCountString = remember(appointmentsState) {
        when (val state = appointmentsState) {
            is AppointmentListState.Success -> state.appointments.size.toString()
            else -> "0"
        }
    }
    val reviewsCountString = remember(appointmentsState) {
        when (val state = appointmentsState) {
            is AppointmentListState.Success -> {
                // Dynamic reviews count = completed appointments (or accepted if completed is empty)
                val completedCount = state.appointments.count { it.status == "completed" }
                if (completedCount > 0) completedCount.toString()
                else state.appointments.count { it.status == "accepted" }.toString()
            }
            else -> "0"
        }
    }
    val doctorPatientsCountString = remember(appointmentsState) {
        when (val state = appointmentsState) {
            is AppointmentListState.Success -> {
                state.appointments.map { it.patientId }.distinct().size.toString()
            }
            else -> "0"
        }
    }

    // Profile editing states
    var showEditDialog by remember { mutableStateOf(false) }
    var showAvailabilitySettings by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf("") }
    var editPhone by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var selectedImageBytes by remember { mutableStateOf<ByteArray?>(null) }
    var selectedImageName by remember { mutableStateOf<String?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let {
            selectedImageUri = it
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                selectedImageBytes = inputStream?.readBytes()
                selectedImageName = "avatar_${System.currentTimeMillis()}.png"
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Scaffold(
        containerColor = AppBackground,
        bottomBar = {
            BottomNavBar(
                currentRoute = Screen.PatientProfile.route,
                items = if (isDoctor) doctorNavItems else patientNavItems,
                onNavItemClick = { route ->
                    when (route) {
                        Screen.PatientHome.route, Screen.DoctorDashboard.route -> onNavigateToHome()
                        Screen.Search.route, Screen.DoctorHospital.route -> onNavigateToSearch()
                        Screen.PatientAppointments.route, Screen.DoctorAppointments.route -> onNavigateToAppointments()
                        Screen.PatientNotifications.route, Screen.DoctorNotifications.route -> onNavigateToNotifications()
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceWhite)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Replacing static emoji brand icon with logo
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.simats.hospiq.R.drawable.app_logo),
                        contentDescription = "Logo",
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text("HospiQ", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DeepTeal, modifier = Modifier.weight(1f))
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(24.dp))
                // Avatar with edit badge
                Box(
                    contentAlignment = Alignment.BottomEnd,
                    modifier = Modifier.clickable {
                        editName = sessionManager.getName() ?: ""
                        editPhone = sessionManager.getPhone() ?: ""
                        selectedImageUri = null
                        selectedImageBytes = null
                        selectedImageName = null
                        authViewModel.clearError()
                        showEditDialog = true
                    }
                ) {
                    val profilePhotoPath = sessionManager.getProfilePhoto()
                    val imageUrl = if (!profilePhotoPath.isNullOrEmpty()) {
                        "${com.simats.hospiq.network.ApiConfig.IMAGE_BASE_URL}$profilePhotoPath"
                    } else null

                    if (imageUrl != null) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = "Profile Photo",
                            modifier = Modifier
                                .size(88.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .background(if (isDoctor) IndigoDoctor else DeepTeal, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(initials, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 32.sp)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .background(if (isDoctor) DeepTeal else IndigoDoctor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(userName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = CharcoalText)
                Text(subtitle, fontSize = 14.sp, color = SlateGray)

                Spacer(Modifier.height(20.dp))

                // Stats row
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        if (isDoctor) {
                            ProfileStat(apptCountString, "Appts")
                            VerticalDivider(modifier = Modifier.height(40.dp), color = BorderGray)
                            ProfileStat("4.9", "Rating")
                            VerticalDivider(modifier = Modifier.height(40.dp), color = BorderGray)
                            ProfileStat(doctorPatientsCountString, "Patients")
                        } else {
                            // Hides the middle "Hospitals" column for patients, making counts clean and dynamic
                            ProfileStat(apptCountString, "Appts")
                            VerticalDivider(modifier = Modifier.height(40.dp), color = BorderGray)
                            ProfileStat(reviewsCountString, "Reviews")
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Menu card 1 (Only show for Doctors. Hidden completely for Patients)
                if (isDoctor) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column {
                            ProfileMenuItem(
                                icon = Icons.Default.DateRange, 
                                label = "Create Slots", 
                                onClick = { showAvailabilitySettings = true }
                            )
                            HorizontalDivider(color = BorderGray)
                            ProfileMenuItem(icon = Icons.Default.LocalHospital, label = "Hospital Profile", onClick = {})
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }

                // Menu card 2
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column {
                        ProfileMenuItem(icon = Icons.Default.Settings, label = "Settings", onClick = onNavigateToSettings)
                        HorizontalDivider(color = BorderGray)
                        ProfileMenuItem(
                            icon = Icons.AutoMirrored.Filled.Logout,
                            label = "Log Out",
                            onClick = onLogout,
                            labelColor = CoralOrange
                        )
                    }
                }

                Spacer(Modifier.height(40.dp))
            }
        }
    }

    if (showAvailabilitySettings) {
        CustomSlotCreatorDialog(
            doctorViewModel = doctorViewModel,
            doctorId = doctorId,
            onDismiss = { showAvailabilitySettings = false }
        )
    }

    // Edit Profile Dialog
    if (showEditDialog) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { if (!authViewModel.isLoading) showEditDialog = false }
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Edit Profile",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalText
                    )
                    Spacer(Modifier.height(16.dp))

                    // Dialog image preview
                    Box(
                        contentAlignment = Alignment.BottomEnd,
                        modifier = Modifier.clickable {
                            imagePickerLauncher.launch("image/*")
                        }
                    ) {
                        val profilePhotoPath = sessionManager.getProfilePhoto()
                        val currentImageUrl = if (!profilePhotoPath.isNullOrEmpty()) {
                            "${com.simats.hospiq.network.ApiConfig.IMAGE_BASE_URL}$profilePhotoPath"
                        } else null

                        if (selectedImageUri != null) {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "Selected Photo",
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else if (currentImageUrl != null) {
                            AsyncImage(
                                model = currentImageUrl,
                                contentDescription = "Current Photo",
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .background(DeepTeal, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(initials, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 28.sp)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(IndigoDoctor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CameraAlt, null, tint = Color.White, modifier = Modifier.size(12.dp))
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = { imagePickerLauncher.launch("image/*") }) {
                        Text("Change Photo", color = DeepTeal, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DeepTeal,
                            unfocusedBorderColor = BorderGray
                        )
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("Phone Number") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DeepTeal,
                            unfocusedBorderColor = BorderGray
                        )
                    )

                    Spacer(Modifier.height(20.dp))

                    if (authViewModel.errorMessage != null) {
                        Text(
                            text = authViewModel.errorMessage ?: "",
                            color = CoralOrange,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { showEditDialog = false },
                            enabled = !authViewModel.isLoading
                        ) {
                            Text("Cancel", color = SlateGray)
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = {
                                authViewModel.updateProfile(
                                    userId = sessionManager.getUserId(),
                                    fullName = editName,
                                    phone = editPhone,
                                    photoBytes = selectedImageBytes,
                                    photoFileName = selectedImageName,
                                    sessionManager = sessionManager,
                                    onSuccess = {
                                        showEditDialog = false
                                        selectedImageBytes = null
                                        selectedImageUri = null
                                    }
                                )
                            },
                            enabled = !authViewModel.isLoading && editName.isNotBlank() && editPhone.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = DeepTeal),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (authViewModel.isLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                            } else {
                                Text("Save", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DeepTeal)
        Text(label, fontSize = 12.sp, color = SlateGray)
    }
}

@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    labelColor: Color = CharcoalText
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() } // Correctly resolves the non-responsive logout button bug!
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = if (labelColor == CharcoalText) SlateGray else labelColor, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(14.dp))
        Text(label, fontSize = 15.sp, color = labelColor, modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, null, tint = BorderGray, modifier = Modifier.size(20.dp))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CustomSlotCreatorDialog(
    doctorViewModel: DoctorViewModel,
    doctorId: Int,
    onDismiss: () -> Unit
) {
    var isSaving by remember { mutableStateOf(false) }
    var applyTo by remember { mutableStateOf("all_days") } // "all_days" or "specific_date"

    val calendar = Calendar.getInstance()
    var selectedDate by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)) }

    val context = LocalContext.current
    fun showDatePicker() {
        val datePickerDialog = android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val cal = Calendar.getInstance()
                cal.set(year, month, dayOfMonth)
                selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
        datePickerDialog.show()
    }

    // Dynamic timings array
    val initialTimings = doctorViewModel.profileState.value.let { state ->
        if (state is DoctorProfileState.Success && state.slots.isNotEmpty()) {
            state.slots.map { it.slotTime.take(5) }.distinct()
        } else {
            listOf("09:00", "09:30", "10:00", "10:30", "11:00", "14:00", "14:30", "15:00", "15:30", "16:00")
        }
    }
    val timings = remember { mutableStateListOf(*initialTimings.toTypedArray()) }

    fun showTimePicker() {
        val timePickerDialog = android.app.TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                val timeString = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)
                if (!timings.contains(timeString)) {
                    timings.add(timeString)
                    timings.sort()
                }
            },
            9, 0, false
        )
        timePickerDialog.show()
    }

    androidx.compose.ui.window.Dialog(onDismissRequest = { if (!isSaving) onDismiss() }) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = AppBackground),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(16.dp)
                .heightIn(max = 600.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Create Custom Slots",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalText
                    )
                    IconButton(onClick = onDismiss, enabled = !isSaving) {
                        Text("✕", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SlateGray)
                    }
                }

                Text(
                    "Set up your custom booking slots. You can apply these to all repetitive days or override a specific date.",
                    fontSize = 12.sp,
                    color = SlateGray
                )

                // Apply To Selection
                Text("Apply To", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CharcoalText)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = applyTo == "all_days",
                        onClick = { applyTo = "all_days" },
                        label = { Text("All Repetitive Days", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = SoftTeal, selectedLabelColor = DeepTeal),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = applyTo == "specific_date",
                        onClick = { applyTo = "specific_date" },
                        label = { Text("Specific Date", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = SoftTeal, selectedLabelColor = DeepTeal),
                        modifier = Modifier.weight(1f)
                    )
                }

                if (applyTo == "specific_date") {
                    OutlinedTextField(
                        value = selectedDate,
                        onValueChange = {},
                        label = { Text("Select Date") },
                        readOnly = true,
                        trailingIcon = { Icon(Icons.Default.DateRange, "Pick Date", tint = DeepTeal, modifier = Modifier.clickable { showDatePicker() }) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Custom Timings", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CharcoalText)
                    TextButton(onClick = { showTimePicker() }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Slot", modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Add a Slot")
                    }
                }

                if (timings.isEmpty()) {
                    Text("No slots defined. Add a slot to begin.", fontSize = 13.sp, color = SlateGray, modifier = Modifier.padding(vertical = 12.dp))
                } else {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        timings.forEach { time ->
                            Box(
                                modifier = Modifier
                                    .background(SoftTeal, RoundedCornerShape(16.dp))
                                    .clickable { timings.remove(time) }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(time, fontSize = 14.sp, color = DeepTeal, fontWeight = FontWeight.SemiBold)
                                    Spacer(Modifier.width(6.dp))
                                    Icon(Icons.Default.Close, contentDescription = "Remove", tint = DeepTeal, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))

                // Save/Close Button
                Button(
                    onClick = {
                        isSaving = true
                        doctorViewModel.createCustomSlots(
                            doctorId = doctorId,
                            applyTo = applyTo,
                            targetDate = selectedDate,
                            timings = timings.toList().filter { it.isNotBlank() },
                            onDone = {
                                isSaving = false
                                onDismiss()
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DeepTeal),
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                    } else {
                        Text("Save & Apply Slots", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
