package com.simats.hospiq.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import com.simats.hospiq.network.ApiConfig
import com.simats.hospiq.navigation.Screen
import com.simats.hospiq.ui.components.*
import com.simats.hospiq.ui.theme.*
import com.simats.hospiq.network.models.Appointment
import com.simats.hospiq.utils.SessionManager
import com.simats.hospiq.viewmodels.HospitalUiState
import com.simats.hospiq.viewmodels.HospitalViewModel
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import com.google.android.gms.location.LocationServices

// ─────────────────────────────────────────────────────────────────────────────
// Root screen composable
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun PatientHomeScreen(
    sessionManager: SessionManager,
    hospitalViewModel: HospitalViewModel,
    appointmentViewModel: com.simats.hospiq.viewmodels.AppointmentViewModel,
    onNavigateToHospitalDetail: (Int, Double?, Double?) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToAppointments: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val firstName = sessionManager.getName()?.split(" ")?.firstOrNull() ?: "there"
    val hospitalState by hospitalViewModel.listState.collectAsState()
    val nearbyHospitals = when (val s = hospitalState) {
        is HospitalUiState.Success -> s.nearbyHospitals
        else -> emptyList()
    }
    val topRatedHospitals = when (val s = hospitalState) {
        is HospitalUiState.Success -> s.topRatedHospitals
        else -> emptyList()
    }
    val unreadCount = 0 // Will be driven by notification badge from backend

    val appointmentsState by appointmentViewModel.appointmentsState.collectAsState()
    val appointments = when (val state = appointmentsState) {
        is com.simats.hospiq.viewmodels.AppointmentListState.Success -> state.appointments
        else -> emptyList()
    }

    var userLatitude by remember { mutableStateOf<Double?>(null) }
    var userLongitude by remember { mutableStateOf<Double?>(null) }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineLocationGranted || coarseLocationGranted) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                    if (loc != null) {
                        userLatitude = loc.latitude
                        userLongitude = loc.longitude
                        hospitalViewModel.loadHospitals(loc.latitude, loc.longitude)
                    } else {
                        // Fallback Chennai
                        userLatitude = 13.0827
                        userLongitude = 80.2707
                        hospitalViewModel.loadHospitals(13.0827, 80.2707)
                    }
                }
            } catch (e: SecurityException) {
                userLatitude = 13.0827
                userLongitude = 80.2707
                hospitalViewModel.loadHospitals(13.0827, 80.2707)
            }
        } else {
            userLatitude = 13.0827
            userLongitude = 80.2707
            hospitalViewModel.loadHospitals(13.0827, 80.2707)
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
        appointmentViewModel.loadPatientAppointments(sessionManager.getUserId())
    }

    var showMapDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = AppBackground,
        bottomBar = {
            BottomNavBar(
                currentRoute = Screen.PatientHome.route,
                items = patientNavItems,
                onNavItemClick = { route ->
                    when (route) {
                        Screen.Search.route              -> onNavigateToSearch()
                        Screen.PatientAppointments.route -> onNavigateToAppointments()
                        Screen.PatientNotifications.route -> onNavigateToNotifications()
                        Screen.PatientProfile.route      -> onNavigateToProfile()
                    }
                }
            )
        }
    ) { innerPadding ->
        // Show full-page spinner ONLY when hospitals are loading AND we have no data yet
        val isFirstLoad = hospitalState is HospitalUiState.Loading && nearbyHospitals.isEmpty() && topRatedHospitals.isEmpty()

        if (isFirstLoad) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    CircularProgressIndicator(color = DeepTeal)
                    Text("Finding hospitals near you...", color = SlateGray, fontSize = 14.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
            // ── Top Bar ────────────────────────────────────────────
            item {
                HomeTopBar(
                    firstName = firstName,
                    profilePhotoPath = sessionManager.getProfilePhoto(),
                    unreadNotifications = unreadCount,
                    onNotificationsClick = onNavigateToNotifications,
                    onProfileClick = onNavigateToProfile
                )
            }

            // ── Search Bar ──────────────────────────────────────────
            item {
                HomeSearchBar(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    onSearchClick = onNavigateToSearch
                )
            }

            // ── Near You section ───────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionHeader(
                        title = "Near you",
                        actionLabel = "See All",
                        onActionClick = onNavigateToSearch
                    )
                    // Map toggle button
                    OutlinedButton(
                        onClick = { showMapDialog = true },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DeepTeal),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.Map, null, tint = DeepTeal, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Map", color = DeepTeal, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            item {
                if (hospitalState is HospitalUiState.Loading) {
                    // Skeleton while refreshing
                    Box(modifier = Modifier.padding(horizontal = 16.dp).height(160.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = DeepTeal, strokeWidth = 2.dp)
                    }
                } else if (nearbyHospitals.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        Text("No hospitals found nearby. Try expanding your search.", color = SlateGray, fontSize = 13.sp)
                    }
                } else {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(nearbyHospitals) { hospital ->
                            HospitalCard(
                                hospital = hospital,
                                onClick = { onNavigateToHospitalDetail(hospital.id, userLatitude, userLongitude) },
                                modifier = Modifier.width(260.dp)
                            )
                        }
                    }
                }
            }

            // ── Recent Appointments section ──────────────────────────
            val activeAppointments = appointments.filter { it.status != "cancelled" && it.status != "rejected" }
            item {
                SectionHeader(
                    title = "Recent appointments",
                    actionLabel = "See All",
                    onActionClick = onNavigateToAppointments,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
            when (appointmentsState) {
                is com.simats.hospiq.viewmodels.AppointmentListState.Loading -> {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = DeepTeal, strokeWidth = 2.dp)
                        }
                    }
                }
                else -> {
                    if (activeAppointments.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp).fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("📅", fontSize = 28.sp)
                                    Spacer(Modifier.height(8.dp))
                                    Text("No upcoming appointments", fontWeight = FontWeight.SemiBold, color = CharcoalText)
                                    Text("Book your first appointment today!", fontSize = 13.sp, color = SlateGray)
                                }
                            }
                        }
                    } else {
                        item {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(activeAppointments) { appt ->
                                    RecentAppointmentItem(
                                        appointment = appt,
                                        onNavigateToAppointments = onNavigateToAppointments
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── Doctor's Advice & Prescriptions section ───────────────
            val adviceAppointments = appointments.filter { !it.doctorAdvice.isNullOrBlank() }
            if (adviceAppointments.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Doctor's advice & prescriptions",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }
                items(adviceAppointments) { appt ->
                    DoctorAdviceCard(
                        appointment = appt,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }

            // ── Browse by Specialty ─────────────────────────────────
            item {
                SectionHeader(
                    title = "Browse by specialty",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
            item {
                SpecialtyGrid(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    onSpecialtyClick = onNavigateToSearch
                )
            }

            // ── Top Rated Hospitals ────────────────────────────────
            item {
                SectionHeader(
                    title = "Top rated hospitals",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
            items(topRatedHospitals) { hospital ->
                HospitalCard(
                    hospital = hospital,
                    onClick = { onNavigateToHospitalDetail(hospital.id, userLatitude, userLongitude) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }
    }

    if (showMapDialog) {
        val uniqueHops = (nearbyHospitals + topRatedHospitals).distinctBy { it.id }
        HospitalsMapDialog(
            context = context,
            hospitals = uniqueHops,
            userLat = userLatitude,
            userLng = userLongitude,
            onNavigateToHospitalDetail = { id -> onNavigateToHospitalDetail(id, userLatitude, userLongitude) },
            onDismiss = { showMapDialog = false }
        )
    }
}
}

// ─────────────────────────────────────────────────────────────────────────────
// Top Bar
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun HomeTopBar(
    firstName: String,
    profilePhotoPath: String?,
    unreadNotifications: Int,
    onNotificationsClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = SurfaceWhite,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar + greeting
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Dynamic Profile avatar
                val imageUrl = if (!profilePhotoPath.isNullOrEmpty()) {
                    "${ApiConfig.IMAGE_BASE_URL}$profilePhotoPath"
                } else null

                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(DeepTeal)
                        .clickable { onProfileClick() },
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
                        Text(
                            text = firstName.take(1).uppercase(),
                            color = SurfaceWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
                Column {
                    Text(
                        text = "Good morning, $firstName 👋",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalText
                    )
                    Text(
                        text = "How are you today?",
                        fontSize = 13.sp,
                        color = SlateGray
                    )
                }
            }

            // Notification bell with badge
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(SoftTeal)
                    .clickable { onNotificationsClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = DeepTeal,
                    modifier = Modifier.size(22.dp)
                )
                if (unreadNotifications > 0) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(CoralOrange, CircleShape)
                            .align(Alignment.TopEnd)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Search Bar
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun HomeSearchBar(
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onSearchClick() },
        shape = RoundedCornerShape(14.dp),
        color = BorderGray.copy(alpha = 0.5f),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = SlateGray,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = "Search hospitals, doctors...",
                fontSize = 14.sp,
                color = SlateGray,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = "Filter",
                tint = DeepTeal,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Section Header
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun SectionHeader(
    title: String,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = CharcoalText
        )
        if (actionLabel != null && onActionClick != null) {
            Text(
                text = actionLabel,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = DeepTeal,
                modifier = Modifier.clickable { onActionClick() }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Specialty Grid  (8 items in 4 rows × 2 cols)
// ─────────────────────────────────────────────────────────────────────────────

private data class SpecialtyItem(val emoji: String, val label: String)

private val specialtyItems = listOf(
    SpecialtyItem("🫀", "Cardiology"),
    SpecialtyItem("👁", "Ophthalmology"),
    SpecialtyItem("❤️", "Oncology"),
    SpecialtyItem("🦷", "Dental"),
    SpecialtyItem("🧠", "Neurology"),
    SpecialtyItem("🧘", "Psychiatry"),
    SpecialtyItem("👶", "Pediatrics"),
    SpecialtyItem("➕", "More")
)

@Composable
fun SpecialtyGrid(
    onSpecialtyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        for (row in 0 until 4) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                for (col in 0 until 2) {
                    val index = row * 2 + col
                    val item = specialtyItems[index]
                    SpecialtyGridItem(
                        emoji = item.emoji,
                        label = item.label,
                        onClick = onSpecialtyClick,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun SpecialtyGridItem(
    emoji: String,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceWhite)
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(SoftTeal, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emoji, fontSize = 22.sp)
        }
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = CharcoalText
        )
    }
}

@Composable
fun StatusPulseIndicator(status: String) {
    val color = when (status.lowercase()) {
        "available" -> androidx.compose.ui.graphics.Color(0xFF4CAF50)
        "busy" -> androidx.compose.ui.graphics.Color(0xFFFF9800)
        "in_surgery" -> androidx.compose.ui.graphics.Color(0xFFE91E63)
        else -> androidx.compose.ui.graphics.Color(0xFF9E9E9E)
    }
    val label = when (status.lowercase()) {
        "available" -> "Available"
        "busy" -> "Busy"
        "in_surgery" -> "In Surgery"
        else -> "Offline"
    }

    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(1000, easing = androidx.compose.animation.core.FastOutSlowInEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        )
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = alpha))
            )
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f * alpha))
                    .align(Alignment.Center)
            )
        }
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun RecentAppointmentItem(
    appointment: Appointment,
    onNavigateToAppointments: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .width(280.dp)
            .clickable { onNavigateToAppointments() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(SoftTeal, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = appointment.doctorName.takeLast(2).uppercase(),
                    color = DeepTeal,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = appointment.doctorName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = CharcoalText
                )
                Text(
                    text = appointment.specialization.replaceFirstChar { it.uppercase() },
                    fontSize = 12.sp,
                    color = SlateGray
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, contentDescription = "Date", tint = DeepTeal, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(appointment.date, fontSize = 11.sp, color = CharcoalText)
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Default.AccessTime, contentDescription = "Time", tint = DeepTeal, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(appointment.time, fontSize = 11.sp, color = CharcoalText)
                }
                Spacer(Modifier.height(6.dp))
                StatusPulseIndicator(status = appointment.doctorStatus ?: "available")
            }
        }
    }
}

@Composable
fun DoctorAdviceCard(
    appointment: Appointment,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SoftTeal.copy(alpha = 0.4f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, DeepTeal.copy(alpha = 0.3f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MedicalServices,
                        contentDescription = "Medical Service",
                        tint = DeepTeal,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Advice from ${appointment.doctorName}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = CharcoalText
                    )
                }
                Box(
                    modifier = Modifier
                        .background(DeepTeal.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = appointment.illnessName?.ifEmpty { "General Consult" } ?: "General Consult",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepTeal
                    )
                }
            }

            HorizontalDivider(color = DeepTeal.copy(alpha = 0.1f))

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Illness details:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SlateGray
                )
                Text(
                    text = appointment.illnessDescription?.ifEmpty { "No description provided." } ?: "No description provided.",
                    fontSize = 12.sp,
                    color = CharcoalText
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceWhite.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Advice",
                        tint = CoralOrange,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Treatment & Prescription:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CoralOrange
                    )
                }
                Text(
                    text = appointment.doctorAdvice ?: "",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = CharcoalText
                )
            }
        }
    }
}
