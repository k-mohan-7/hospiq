package com.simats.hospiq.ui.screens.notifications

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.hospiq.navigation.Screen
import com.simats.hospiq.network.models.AppNotification
import com.simats.hospiq.ui.components.*
import com.simats.hospiq.ui.theme.*
import com.simats.hospiq.utils.DemoData
import com.simats.hospiq.utils.SessionManager
import com.simats.hospiq.viewmodels.NotificationState
import com.simats.hospiq.viewmodels.NotificationViewModel

@Composable
fun PatientNotificationsScreen(
    sessionManager: SessionManager,
    notificationViewModel: NotificationViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToAppointments: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Unread", "Appointments", "Reports")

    val notifState by notificationViewModel.state.collectAsState()
    LaunchedEffect(Unit) { notificationViewModel.loadNotifications(sessionManager.getUserId()) }

    val allNotifs = when (val s = notifState) {
        is NotificationState.Success -> s.notifications
        else -> DemoData.notifications
    }
    val filtered = when (selectedFilter) {
        "Unread" -> allNotifs.filter { !it.isRead }
        "Appointments" -> allNotifs.filter { it.type == "appointment" }
        "Reports" -> allNotifs.filter { it.type == "lab" }
        else -> allNotifs
    }

    Scaffold(
        containerColor = AppBackground,
        bottomBar = {
            BottomNavBar(
                currentRoute = Screen.PatientNotifications.route,
                items = patientNavItems,
                onNavItemClick = { route ->
                    when (route) {
                        Screen.PatientHome.route -> onNavigateToHome()
                        Screen.Search.route -> onNavigateToSearch()
                        Screen.PatientAppointments.route -> onNavigateToAppointments()
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
                        .background(DeepTeal, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(sessionManager.getInitials(), color = SurfaceWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            // Filter chips
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filters.forEach { filter ->
                    val isSelected = selectedFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter, fontSize = 13.sp) },
                        shape = RoundedCornerShape(20.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DeepTeal,
                            selectedLabelColor = SurfaceWhite
                        )
                    )
                }
            }

            if (filtered.isEmpty()) {
                EmptyState(title = "No notifications", subtitle = "You're all caught up!", modifier = Modifier.weight(1f))
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val recent = filtered.filter { it.id <= 2 }
                    val earlier = filtered.filter { it.id > 2 }

                    if (recent.isNotEmpty()) {
                        item {
                            Text("RECENT", fontSize = 11.sp, color = SlateGray, fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(vertical = 4.dp))
                        }
                        items(recent) { notif -> NotificationRow(notif) }
                    }
                    if (earlier.isNotEmpty()) {
                        item {
                            Spacer(Modifier.height(4.dp))
                            Text("EARLIER TODAY", fontSize = 11.sp, color = SlateGray, fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(vertical = 4.dp))
                        }
                        items(earlier) { notif -> NotificationRow(notif) }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationRow(notification: AppNotification) {
    val (bgColor, iconEmoji) = when (notification.type) {
        "appointment" -> if (!notification.isRead) SoftTeal to "📅" else AppBackground to "📅"
        "rating" -> AppBackground to "⭐"
        "lab" -> AppBackground to "🔬"
        else -> AppBackground to "ℹ️"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor, RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(if (!notification.isRead) DeepTeal else BorderGray, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(iconEmoji, fontSize = 20.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(notification.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CharcoalText)
                Text(notification.timeAgo, fontSize = 11.sp, color = SlateGray)
            }
            Spacer(Modifier.height(4.dp))
            Text(notification.body, fontSize = 13.sp, color = SlateGray, lineHeight = 19.sp)
            if (!notification.isRead) {
                Spacer(Modifier.height(6.dp))
                Text(
                    "NEW",
                    fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MintGreen,
                    modifier = Modifier
                        .background(MintWash, RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}
