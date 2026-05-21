package com.simats.hospiq.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.hospiq.navigation.Screen
import com.simats.hospiq.ui.theme.*

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

val patientNavItems = listOf(
    BottomNavItem("Home", Icons.Default.Home, Screen.PatientHome.route),
    BottomNavItem("Search", Icons.Default.Search, Screen.Search.route),
    BottomNavItem("Schedule", Icons.Default.DateRange, Screen.PatientAppointments.route),
    BottomNavItem("Alerts", Icons.Default.Notifications, Screen.PatientNotifications.route),
    BottomNavItem("Profile", Icons.Default.Person, Screen.PatientProfile.route)
)

val doctorNavItems = listOf(
    BottomNavItem("Home", Icons.Default.Home, Screen.DoctorDashboard.route),
    BottomNavItem("Hospital", Icons.Default.LocalHospital, Screen.DoctorHospital.route),
    BottomNavItem("Schedule", Icons.Default.DateRange, Screen.DoctorAppointments.route),
    BottomNavItem("Alerts", Icons.Default.Notifications, Screen.DoctorNotifications.route),
    BottomNavItem("Profile", Icons.Default.Person, Screen.PatientProfile.route)
)

@Composable
fun BottomNavBar(
    currentRoute: String,
    items: List<BottomNavItem>,
    onNavItemClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = SurfaceWhite,
        tonalElevation = 0.dp
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = { onNavItemClick(item.route) },
                icon = {
                    if (selected) {
                        Box(
                            modifier = Modifier
                                .background(DeepTeal, RoundedCornerShape(14.dp))
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = SurfaceWhite,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = SlateGray,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                },
                label = {
                    Text(
                        text = item.label,
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (selected) DeepTeal else SlateGray
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = SurfaceWhite
                )
            )
        }
    }
}
