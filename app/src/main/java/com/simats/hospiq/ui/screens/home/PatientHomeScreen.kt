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
import com.simats.hospiq.network.ApiConfig
import com.simats.hospiq.navigation.Screen
import com.simats.hospiq.ui.components.*
import com.simats.hospiq.ui.theme.*
import com.simats.hospiq.utils.DemoData
import com.simats.hospiq.utils.SessionManager
import com.simats.hospiq.viewmodels.HospitalUiState
import com.simats.hospiq.viewmodels.HospitalViewModel

// ─────────────────────────────────────────────────────────────────────────────
// Root screen composable
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun PatientHomeScreen(
    sessionManager: SessionManager,
    hospitalViewModel: HospitalViewModel,
    onNavigateToHospitalDetail: (Int) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToAppointments: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val firstName = sessionManager.getName()?.split(" ")?.firstOrNull() ?: "there"
    val hospitalState by hospitalViewModel.listState.collectAsState()
    val nearbyHospitals = when (val s = hospitalState) {
        is HospitalUiState.Success -> s.nearbyHospitals
        else -> DemoData.hospitals.take(3)
    }
    val topRatedHospitals = when (val s = hospitalState) {
        is HospitalUiState.Success -> s.topRatedHospitals
        else -> DemoData.hospitals
    }
    val unreadCount = DemoData.notifications.count { !it.isRead }

    LaunchedEffect(Unit) { hospitalViewModel.loadHospitals() }

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
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // ── Top Bar ──────────────────────────────────────────────────
            item {
                HomeTopBar(
                    firstName = firstName,
                    profilePhotoPath = sessionManager.getProfilePhoto(),
                    unreadNotifications = unreadCount,
                    onNotificationsClick = onNavigateToNotifications,
                    onProfileClick = onNavigateToProfile
                )
            }

            // ── Search Bar ───────────────────────────────────────────────
            item {
                HomeSearchBar(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    onSearchClick = onNavigateToSearch
                )
            }

            // ── Near You section ─────────────────────────────────────────
            item {
                SectionHeader(
                    title = "Near you",
                    actionLabel = "See All",
                    onActionClick = onNavigateToSearch,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(nearbyHospitals) { hospital ->
                        HospitalCard(
                            hospital = hospital,
                            onClick = { onNavigateToHospitalDetail(hospital.id) },
                            modifier = Modifier.width(260.dp)
                        )
                    }
                }
            }

            // ── Browse by Specialty ──────────────────────────────────────
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

            // ── Top Rated Hospitals ──────────────────────────────────────
            item {
                SectionHeader(
                    title = "Top rated hospitals",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
            items(topRatedHospitals) { hospital ->
                HospitalCard(
                    hospital = hospital,
                    onClick = { onNavigateToHospitalDetail(hospital.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
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
