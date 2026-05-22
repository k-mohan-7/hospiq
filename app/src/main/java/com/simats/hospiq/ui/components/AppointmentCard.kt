package com.simats.hospiq.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.hospiq.network.models.Appointment
import com.simats.hospiq.ui.theme.*

@Composable
fun AppointmentCard(
    appointment: Appointment,
    isDoctor: Boolean = false,
    onAction1: (() -> Unit)? = null,
    action1Label: String? = null,
    onAction2: (() -> Unit)? = null,
    action2Label: String? = null,
    onCardClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val barColor = when (appointment.status.lowercase()) {
        "accepted"   -> MintGreen
        "completed"  -> DeepTeal
        "cancelled"  -> CoralOrange
        "pending"    -> AmberStar
        else         -> SlateGray
    }

    val cardModifier = if (onCardClick != null) {
        modifier.fillMaxWidth().clickable { onCardClick() }
    } else {
        modifier.fillMaxWidth()
    }

    Card(
        modifier = cardModifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Max)) {
            // Colored left bar
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(barColor, RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
            )

            Column(modifier = Modifier.padding(14.dp).fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(IndigoLight, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        val nameStr = if (isDoctor) appointment.patientName else appointment.doctorName
                        val initials = nameStr.trim().split(" ")
                            .take(2).joinToString("") { it.first().uppercaseChar().toString() }
                        Text(text = initials, color = IndigoDoctor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isDoctor) appointment.patientName else appointment.doctorName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = CharcoalText
                        )
                        Text(
                            text = "${appointment.specialization} • ${appointment.hospitalName}",
                            fontSize = 12.sp,
                            color = SlateGray,
                            maxLines = 1
                        )
                    }
                    StatusBadge(status = appointment.status)
                }

                Spacer(Modifier.height(10.dp))
                HorizontalDivider(color = BorderGray, thickness = 0.5.dp)
                Spacer(Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("📅 ${appointment.date}", fontSize = 13.sp, color = SlateGray)
                    Spacer(Modifier.width(12.dp))
                    Text("🕐 ${appointment.time}", fontSize = 13.sp, color = SlateGray)
                }

                if (!isDoctor) {
                    Spacer(Modifier.height(8.dp))
                    val status = appointment.doctorStatus ?: "available"
                    val (statusColor, statusLabel, statusEmoji) = when (status.lowercase()) {
                        "available" -> Triple(Color(0xFF4CAF50), "Available", "🟢")
                        "busy" -> Triple(Color(0xFFFF9800), "Busy", "🕐")
                        "in_surgery" -> Triple(Color(0xFFE91E63), "In Surgery", "🔴")
                        else -> Triple(Color(0xFF9E9E9E), "Offline", "⚫")
                    }

                    Box(
                        modifier = Modifier
                            .background(statusColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(statusEmoji, fontSize = 11.sp)
                            Text(
                                text = "Doctor is $statusLabel",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                        }
                    }
                }

                if (onAction1 != null && action1Label != null) {
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { onAction1() },
                            colors = ButtonDefaults.buttonColors(containerColor = DeepTeal),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(text = action1Label, fontSize = 13.sp, color = SurfaceWhite)
                        }
                        if (onAction2 != null && action2Label != null) {
                            OutlinedButton(
                                onClick = { onAction2() },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray)
                            ) {
                                Text(text = action2Label, fontSize = 13.sp, color = CharcoalText)
                            }
                        }
                    }
                }
            }
        }
    }
}
