package com.simats.hospiq.ui.screens.booking

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.hospiq.ui.theme.*
import com.simats.hospiq.utils.DemoData
import com.simats.hospiq.utils.SessionManager
import com.simats.hospiq.viewmodels.AppointmentViewModel

@Composable
fun AppointmentConfirmScreen(
    appointmentId: Int,
    appointmentViewModel: AppointmentViewModel,
    sessionManager: SessionManager,
    onViewAppointments: () -> Unit,
    onGoHome: () -> Unit
) {
    val appointment = DemoData.patientAppointments.find { it.id == appointmentId }
        ?: DemoData.patientAppointments.first()
    val doctor = DemoData.doctors.find { it.name == appointment.doctorName }
        ?: DemoData.doctors.first()

    // Animate checkmark
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "checkScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(40.dp))

            // Checkmark circle
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .scale(scale)
                    .background(DeepTeal, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(52.dp)
                )
            }

            Spacer(Modifier.height(24.dp))
            Text(
                "Appointment confirmed!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = DeepTeal,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            // Summary card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .background(IndigoLight, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            val initials = appointment.doctorName.trim().split(" ")
                                .take(2).joinToString("") { it.first().uppercaseChar().toString() }
                            Text(initials, color = IndigoDoctor, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(appointment.doctorName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = CharcoalText)
                            Text("${appointment.specialization} • ${appointment.hospitalName}", fontSize = 12.sp, color = SlateGray)
                        }
                        Text(
                            "Confirmed",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MintGreen,
                            modifier = Modifier
                                .background(MintWash, RoundedCornerShape(20.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = BorderGray)

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("DATE", fontSize = 11.sp, color = SlateGray, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.DateRange, null, tint = DeepTeal, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(appointment.date, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CharcoalText)
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("TIME", fontSize = 11.sp, color = SlateGray, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccessTime, null, tint = DeepTeal, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(appointment.time, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CharcoalText)
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VideoCall, null, tint = DeepTeal, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (appointment.consultationType == "video_call") "Video Consultation" else "In-Person",
                            fontSize = 14.sp, color = CharcoalText, modifier = Modifier.weight(1f)
                        )
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(MintGreen, CircleShape)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("In-App link sent", fontSize = 12.sp, color = MintGreen, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = onViewAppointments,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DeepTeal)
            ) {
                Icon(Icons.Default.DateRange, null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("View my appointments", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = onGoHome,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray)
            ) {
                Icon(Icons.Default.Home, null, tint = CharcoalText, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Go home", color = CharcoalText, fontWeight = FontWeight.Medium, fontSize = 16.sp)
            }

            Spacer(Modifier.height(24.dp))

            Text(
                "A confirmation email has been sent to your registered address.",
                fontSize = 13.sp, color = SlateGray, textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Need to reschedule?",
                fontSize = 13.sp, color = DeepTeal, fontWeight = FontWeight.SemiBold
            )
        }
    }
}
