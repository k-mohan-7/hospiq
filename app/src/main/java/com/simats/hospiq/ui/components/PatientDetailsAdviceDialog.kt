package com.simats.hospiq.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.simats.hospiq.network.models.Appointment
import com.simats.hospiq.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientDetailsAdviceDialog(
    appointment: Appointment,
    // Pass in the full list of all loaded appointments so we can show real history
    allAppointments: List<Appointment> = emptyList(),
    onDismiss: () -> Unit,
    onSubmitAdvice: (String) -> Unit
) {
    var adviceText by remember { mutableStateOf(appointment.doctorAdvice ?: "") }
    var isSubmitting by remember { mutableStateOf(false) }

    // Build patient history from the real backend data (completed appointments for this patient, excluding the current one)
    val patientHistory = remember(appointment.patientId, allAppointments) {
        allAppointments
            .filter { it.patientId == appointment.patientId && it.status == "completed" && it.id != appointment.id }
            .distinctBy { it.id }
            .take(3)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(24.dp),
            color = AppBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Patient Profile",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = CharcoalText
                        )
                        Text(
                            text = appointment.patientName,
                            fontSize = 14.sp,
                            color = SlateGray
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Text("✕", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SlateGray)
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Appointment Metadata Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Appointment Info", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DeepTeal)
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("📅 Date: ${appointment.date}", fontSize = 13.sp, color = CharcoalText)
                                Text("🕐 Time: ${appointment.time}", fontSize = 13.sp, color = CharcoalText)
                            }
                            Text("🏥 Hospital: ${appointment.hospitalName}", fontSize = 13.sp, color = CharcoalText)
                            Text("🩺 Mode: ${appointment.consultationType.replace("_", " ").uppercase()}", fontSize = 13.sp, color = CharcoalText)
                            Text(
                                "Status: ${appointment.status.replaceFirstChar { it.uppercaseChar() }}",
                                fontSize = 13.sp,
                                color = when (appointment.status) {
                                    "accepted" -> MintGreen
                                    "pending" -> AmberStar
                                    "completed" -> DeepTeal
                                    "cancelled", "rejected" -> CoralOrange
                                    else -> SlateGray
                                },
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Symptoms & Disease Info — loaded from actual DB fields
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Patient Reported Illness & Symptoms", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CoralOrange)
                            
                            Column {
                                Text("Illness Name / Reason", fontSize = 11.sp, color = SlateGray, fontWeight = FontWeight.SemiBold)
                                Text(
                                    text = appointment.illnessName?.ifEmpty { "Not specified" } ?: "Not specified",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = CharcoalText
                                )
                            }
                            
                            HorizontalDivider(color = BorderGray, thickness = 0.5.dp)

                            Column {
                                Text("Symptoms Description", fontSize = 11.sp, color = SlateGray, fontWeight = FontWeight.SemiBold)
                                Text(
                                    text = appointment.illnessDescription?.ifEmpty { "No description provided" } ?: "No description provided",
                                    fontSize = 14.sp,
                                    color = CharcoalText
                                )
                            }

                            HorizontalDivider(color = BorderGray, thickness = 0.5.dp)

                            Column {
                                Text("Precautions Taken", fontSize = 11.sp, color = SlateGray, fontWeight = FontWeight.SemiBold)
                                Text(
                                    text = appointment.precautions?.ifEmpty { "None" } ?: "None",
                                    fontSize = 14.sp,
                                    color = CharcoalText
                                )
                            }
                        }
                    }

                    // Patient Medical History — from the real backend loaded appointments
                    if (patientHistory.isNotEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Patient Medical History (Completed)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DeepTeal)
                                
                                patientHistory.forEachIndexed { index, hist ->
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "📅 ${hist.date}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = CharcoalText
                                            )
                                            Text(
                                                text = hist.illnessName?.ifEmpty { "General checkup" } ?: "General checkup",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = DeepTeal
                                            )
                                        }
                                        if (!hist.illnessDescription.isNullOrEmpty()) {
                                            Text(
                                                text = "Description: ${hist.illnessDescription}",
                                                fontSize = 12.sp,
                                                color = SlateGray
                                            )
                                        }
                                        if (!hist.precautions.isNullOrEmpty()) {
                                            Text(
                                                text = "Precautions: ${hist.precautions}",
                                                fontSize = 12.sp,
                                                color = SlateGray
                                            )
                                        }
                                        if (!hist.doctorAdvice.isNullOrEmpty()) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(SoftTeal, RoundedCornerShape(8.dp))
                                                    .padding(8.dp)
                                            ) {
                                                Text(
                                                    text = "Advice: ${hist.doctorAdvice}",
                                                    fontSize = 12.sp,
                                                    color = DeepTeal,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }
                                    if (index < patientHistory.size - 1) {
                                        HorizontalDivider(color = BorderGray, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 4.dp))
                                    }
                                }
                            }
                        }
                    }

                    // Doctor's Advice & Prescription Input
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Provide Quick Medicine & Medical Advice", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DeepTeal)
                            Text("This advice will be updated instantly on the patient's dashboard for emergencies or consultations.", fontSize = 11.sp, color = SlateGray)
                            
                            OutlinedTextField(
                                value = adviceText,
                                onValueChange = { adviceText = it },
                                placeholder = { Text("Enter prescriptions, dosage, or medical precautions...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = DeepTeal,
                                    unfocusedBorderColor = BorderGray
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Footer Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CharcoalText)
                    ) {
                        Text("Close", fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            isSubmitting = true
                            onSubmitAdvice(adviceText)
                        },
                        enabled = adviceText.trim().isNotEmpty() && !isSubmitting,
                        modifier = Modifier.weight(1.5f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DeepTeal)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                        } else {
                            Text("Submit Advice", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
