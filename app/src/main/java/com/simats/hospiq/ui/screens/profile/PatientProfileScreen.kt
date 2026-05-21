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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.simats.hospiq.navigation.Screen
import com.simats.hospiq.ui.components.*
import com.simats.hospiq.ui.theme.*
import com.simats.hospiq.utils.SessionManager
import com.simats.hospiq.viewmodels.AuthViewModel
import com.simats.hospiq.viewmodels.AppointmentViewModel
import com.simats.hospiq.viewmodels.AppointmentListState

@Composable
fun PatientProfileScreen(
    sessionManager: SessionManager,
    authViewModel: AuthViewModel,
    appointmentViewModel: AppointmentViewModel,
    onLogout: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToAppointments: () -> Unit,
    onNavigateToNotifications: () -> Unit
) {
    val context = LocalContext.current
    val isDoctor = sessionManager.getRole() == "doctor"
    val initials = sessionManager.getInitials()
    val userName = sessionManager.getName() ?: "Guest"
    val subtitle = if (isDoctor) "Specialist Account" else "Patient Account"

    // Load appointments for dynamic counts
    val userId = sessionManager.getUserId()
    LaunchedEffect(userId) {
        if (userId != -1) {
            if (isDoctor) {
                appointmentViewModel.loadDoctorAppointments(userId)
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

    // Profile editing states
    var showEditDialog by remember { mutableStateOf(false) }
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
                            ProfileStat("24", "Appts")
                            VerticalDivider(modifier = Modifier.height(40.dp), color = BorderGray)
                            ProfileStat("4.9", "Rating")
                            VerticalDivider(modifier = Modifier.height(40.dp), color = BorderGray)
                            ProfileStat("18", "Patients")
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
                            ProfileMenuItem(icon = Icons.Default.DateRange, label = "My Availability", onClick = {})
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
                        ProfileMenuItem(icon = Icons.Default.Settings, label = "Settings", onClick = {})
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
