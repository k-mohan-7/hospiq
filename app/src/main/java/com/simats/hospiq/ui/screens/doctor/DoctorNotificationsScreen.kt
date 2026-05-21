package com.simats.hospiq.ui.screens.doctor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

private val doctorNotifications = listOf(
    AppNotification(1, "New appointment request", "Ravi Shankar has requested a consultation for tomorrow at 10:00 AM.", "appointment", false, "2m ago"),
    AppNotification(2, "Appointment reminder", "Your appointment with Meena Devi is in 30 minutes.", "appointment", false, "1h ago"),
    AppNotification(3, "Schedule update approved", "Your slot changes for Wednesday have been approved.", "general", true, "3h ago"),
    AppNotification(4, "Lab results pending review", "3 patient lab reports are awaiting your review.", "lab", true, "Yesterday"),
    AppNotification(5, "New patient registered", "Arjun Kumar has joined as your patient.", "general", true, "Yesterday")
)

@Composable
fun DoctorNotificationsScreen(
    sessionManager: SessionManager,
    notificationViewModel: NotificationViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToAppointments: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToHospital: () -> Unit = {}
) {
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Unread", "Appointments", "Reports")
    val notifState by notificationViewModel.state.collectAsState()
    LaunchedEffect(Unit) { notificationViewModel.loadNotifications(sessionManager.getUserId()) }
    val allNotifications = when (val s = notifState) {
        is NotificationState.Success -> s.notifications
        else -> doctorNotifications
    }
    val filtered = when (selectedFilter) {
        "Unread" -> allNotifications.filter { !it.isRead }
        "Appointments" -> allNotifications.filter { it.type == "appointment" }
        "Reports" -> allNotifications.filter { it.type == "lab" }
        else -> allNotifications
    }

    Scaffold(
        containerColor = AppBackground,
        bottomBar = {
            BottomNavBar(
                currentRoute = Screen.DoctorNotifications.route,
                items = doctorNavItems,
                onNavItemClick = { route ->
                    when (route) {
                        Screen.DoctorDashboard.route -> onNavigateToHome()
                        Screen.DoctorAppointments.route -> onNavigateToAppointments()
                        Screen.DoctorHospital.route -> onNavigateToHospital()
                        Screen.PatientProfile.route -> onNavigateToProfile()
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Row(
                modifier = Modifier.fillMaxWidth().background(SurfaceWhite).padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🏥", fontSize = 22.sp)
                Spacer(Modifier.width(8.dp))
                Text("Notifications", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = CharcoalText, modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier.size(36.dp).background(IndigoDoctor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("AR", color = SurfaceWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                filters.forEach { filter ->
                    val isSelected = selectedFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter, fontSize = 13.sp) },
                        shape = RoundedCornerShape(20.dp),
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = DeepTeal, selectedLabelColor = SurfaceWhite)
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
                        item { Text("RECENT", fontSize = 11.sp, color = SlateGray, fontWeight = FontWeight.SemiBold) }
                        items(recent) { DoctorNotifRow(it) }
                    }
                    if (earlier.isNotEmpty()) {
                        item { Spacer(Modifier.height(4.dp)); Text("EARLIER", fontSize = 11.sp, color = SlateGray, fontWeight = FontWeight.SemiBold) }
                        items(earlier) { DoctorNotifRow(it) }
                    }
                }
            }
        }
    }
}

@Composable
private fun DoctorNotifRow(notification: AppNotification) {
    val bgColor = if (!notification.isRead) SoftTeal else AppBackground
    val emoji = when (notification.type) {
        "appointment" -> "📅"; "lab" -> "🔬"; else -> "ℹ️"
    }
    Row(
        modifier = Modifier.fillMaxWidth().background(bgColor, RoundedCornerShape(14.dp)).padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier.size(44.dp).background(if (!notification.isRead) DeepTeal else BorderGray, CircleShape),
            contentAlignment = Alignment.Center
        ) { Text(emoji, fontSize = 20.sp) }
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
                Text("NEW", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MintGreen,
                    modifier = Modifier.background(MintWash, RoundedCornerShape(6.dp)).padding(horizontal = 6.dp, vertical = 2.dp))
            }
        }
    }
}
