package com.simats.hospiq.ui.components

import androidx.compose.foundation.background
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
    modifier: Modifier = Modifier
) {
    val barColor = when (appointment.status.lowercase()) {
        "accepted"   -> MintGreen
        "completed"  -> DeepTeal
        "cancelled"  -> CoralOrange
        "pending"    -> AmberStar
        else         -> SlateGray
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Row {
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

                Row {
                    Text("📅 ${appointment.date}", fontSize = 13.sp, color = SlateGray)
                    Spacer(Modifier.width(12.dp))
                    Text("🕐 ${appointment.time}", fontSize = 13.sp, color = SlateGray)
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
