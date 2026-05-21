package com.simats.hospiq.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.hospiq.network.models.Doctor
import com.simats.hospiq.ui.theme.*

@Composable
fun DoctorCard(
    doctor: Doctor,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showBookButton: Boolean = true
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar circle with initials
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(IndigoLight, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                val initials = doctor.name.trim().split(" ")
                    .take(2).joinToString("") { it.first().uppercaseChar().toString() }
                Text(
                    text = initials,
                    color = IndigoDoctor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = doctor.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalText
                    )
                }
                Text(
                    text = "${doctor.specialization} • ${doctor.yearsExperience} yrs exp",
                    fontSize = 13.sp,
                    color = SlateGray
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = AmberStar,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = " ${doctor.rating}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CharcoalText
                    )
                    Spacer(Modifier.width(8.dp))
                    // Status dot
                    val statusColor = when (doctor.status) {
                        "available" -> MintGreen
                        "busy" -> AmberStar
                        else -> CoralOrange
                    }
                    val statusText = when (doctor.status) {
                        "available" -> "Available Today"
                        "busy" -> "Busy"
                        else -> "In Surgery"
                    }
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(statusColor, CircleShape)
                    )
                    Text(text = " $statusText", fontSize = 12.sp, color = statusColor)
                }
            }

            if (showBookButton) {
                Spacer(Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "View Doctor",
                    tint = DeepTeal,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
