package com.simats.hospiq.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.hospiq.ui.theme.*
import com.simats.hospiq.utils.SessionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    sessionManager: SessionManager,
    onBackClick: () -> Unit
) {
    var notificationsEnabled by remember { mutableStateOf(sessionManager.isNotificationsEnabled()) }
    var appointmentNotif by remember { mutableStateOf(sessionManager.isAppointmentNotifEnabled()) }
    var statusNotif by remember { mutableStateOf(sessionManager.isStatusNotifEnabled()) }
    var reminderMinutes by remember { mutableStateOf(sessionManager.getNotificationReminderMinutes()) }
    var showReminderDropdown by remember { mutableStateOf(false) }

    val reminderOptions = listOf(5, 10, 15, 30, 60)

    Scaffold(
        containerColor = AppBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Notification Settings",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = CharcoalText
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = CharcoalText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Master toggle
            SettingsCard {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "🔔 Notifications",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalText
                    )
                    Text(
                        "Enable or disable all HospiQ notifications",
                        fontSize = 13.sp,
                        color = SlateGray
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("All Notifications", fontSize = 15.sp, color = CharcoalText)
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = {
                                notificationsEnabled = it
                                sessionManager.setNotificationsEnabled(it)
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = SurfaceWhite, checkedTrackColor = DeepTeal)
                        )
                    }
                }
            }

            // Individual toggles (only active when master is on)
            SettingsCard {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "📋 Notification Types",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalText
                    )
                    Spacer(Modifier.height(6.dp))

                    NotifToggleRow(
                        icon = Icons.Default.DateRange,
                        label = "Appointment updates",
                        subtitle = "Booking confirmations & cancellations",
                        checked = appointmentNotif && notificationsEnabled,
                        enabled = notificationsEnabled,
                        onCheck = {
                            appointmentNotif = it
                            sessionManager.setAppointmentNotifEnabled(it)
                        }
                    )

                    HorizontalDivider(color = BorderGray, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))

                    NotifToggleRow(
                        icon = Icons.Default.MedicalServices,
                        label = "Doctor status changes",
                        subtitle = "When your doctor becomes available/busy",
                        checked = statusNotif && notificationsEnabled,
                        enabled = notificationsEnabled,
                        onCheck = {
                            statusNotif = it
                            sessionManager.setStatusNotifEnabled(it)
                        }
                    )
                }
            }

            // Reminder timing
            SettingsCard {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "⏰ Appointment Reminders",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalText
                    )
                    Text(
                        "Get notified before your appointment",
                        fontSize = 13.sp,
                        color = SlateGray
                    )
                    Spacer(Modifier.height(12.dp))
                    Text("Remind me:", fontSize = 13.sp, color = SlateGray, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        reminderOptions.forEach { mins ->
                            val isSelected = reminderMinutes == mins
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) DeepTeal else SurfaceWhite)
                                    .clickable(enabled = notificationsEnabled) {
                                        reminderMinutes = mins
                                        sessionManager.setNotificationReminderMinutes(mins)
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        "$mins",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = if (isSelected) Color.White else if (notificationsEnabled) CharcoalText else SlateGray
                                    )
                                    Text(
                                        "min",
                                        fontSize = 10.sp,
                                        color = if (isSelected) Color.White.copy(alpha = 0.8f) else SlateGray
                                    )
                                }
                            }
                        }
                    }
                    if (!notificationsEnabled) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "⚠️ Enable notifications to use reminders",
                            fontSize = 12.sp,
                            color = AmberStar
                        )
                    }
                }
            }

            // Info card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SoftTeal),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text("💡", fontSize = 20.sp)
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("How reminders work", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DeepTeal)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "HospiQ checks your upcoming appointments in the background and sends a system notification at the chosen time before your appointment.",
                            fontSize = 13.sp,
                            color = DeepTeal.copy(alpha = 0.8f),
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(1.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}

@Composable
private fun NotifToggleRow(
    icon: ImageVector,
    label: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean,
    onCheck: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(SoftTeal, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = DeepTeal, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = if (enabled) CharcoalText else SlateGray)
                Text(subtitle, fontSize = 12.sp, color = SlateGray)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheck,
            enabled = enabled,
            colors = SwitchDefaults.colors(checkedThumbColor = SurfaceWhite, checkedTrackColor = DeepTeal)
        )
    }
}
