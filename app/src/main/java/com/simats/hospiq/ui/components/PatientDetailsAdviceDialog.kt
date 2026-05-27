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
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.draw.clip
import coil.compose.AsyncImage

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PatientDetailsAdviceDialog(
    appointment: Appointment,
    allAppointments: List<Appointment> = emptyList(),
    healthReports: List<com.simats.hospiq.network.models.HealthReport> = emptyList(),
    onDismiss: () -> Unit,
    onSubmitAdvice: (String) -> Unit = {},
    onSubmitReport: ((healthStatus: String, notes: String, docs: List<ByteArray>, docNames: List<String>) -> Unit)? = null,
    onEditReport: ((reportId: Int, healthStatus: String, notes: String) -> Unit)? = null,
    onDeleteReport: ((reportId: Int) -> Unit)? = null
) {
    val context = LocalContext.current
    var adviceText by remember { mutableStateOf(appointment.doctorAdvice ?: "") }
    var healthStatus by remember { mutableStateOf("Stable") }
    var isSubmitting by remember { mutableStateOf(false) }

    val selectedDocs = remember { mutableStateListOf<android.net.Uri>() }
    val documentBytes = remember { mutableStateListOf<ByteArray>() }
    val documentNames = remember { mutableStateListOf<String>() }

    // Upgraded interactive states
    var expandedReportId by remember { mutableStateOf<Int?>(null) }
    var editingReportId by remember { mutableStateOf<Int?>(null) }
    var editStatus by remember { mutableStateOf("Stable") }
    var editNotes by remember { mutableStateOf("") }
    
    // Auto visible for active consultations, collapsible for past ones
    var isNewReportFormVisible by remember { 
        mutableStateOf(appointment.id > 0 && appointment.status == "accepted") 
    }

    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        uris.forEach { uri ->
            try {
                val bytes = context.contentResolver.openInputStream(uri)?.readBytes()
                if (bytes != null) {
                    selectedDocs.add(uri)
                    documentBytes.add(bytes)
                    documentNames.add("report_${System.currentTimeMillis()}_${selectedDocs.size}.png")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

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
                .fillMaxHeight(0.88f),
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
                    // 1. Appointment Metadata Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Appointment Info", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DeepTeal)
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("📅 Date: ${appointment.date.ifEmpty { "N/A" }}", fontSize = 13.sp, color = CharcoalText)
                                Text("🕐 Time: ${appointment.time.ifEmpty { "N/A" }}", fontSize = 13.sp, color = CharcoalText)
                            }
                            Text("🏥 Hospital: ${appointment.hospitalName.ifEmpty { "N/A" }}", fontSize = 13.sp, color = CharcoalText)
                            Text("🩺 Mode: ${(appointment.consultationType ?: "in_person").replace("_", " ").uppercase()}", fontSize = 13.sp, color = CharcoalText)
                            Text(
                                "Status: ${(appointment.status ?: "active").replaceFirstChar { it.uppercaseChar() }}",
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

                    // 2. Patient Reported Illness & Symptoms (Always Show Details)
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

                    // 3. Patient Completed Medical History (Reverted original design with prescription details)
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

                    // 4. Upgraded Patient Health Reports (From Database)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                "Patient Medical History & Reports (From Database)", 
                                fontSize = 12.sp, 
                                fontWeight = FontWeight.Bold, 
                                color = DeepTeal
                            )
                            
                            if (healthReports.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("📄", fontSize = 28.sp)
                                        Spacer(Modifier.height(4.dp))
                                        Text("No health reports recorded", fontSize = 12.sp, color = SlateGray, fontWeight = FontWeight.Medium)
                                    }
                                }
                            } else {
                                healthReports.forEachIndexed { index, report ->
                                    val isExpanded = expandedReportId == report.id
                                    val isEditing = editingReportId == report.id
                                    
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isExpanded) SoftTeal.copy(alpha = 0.15f) else AppBackground.copy(alpha = 0.6f)
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { 
                                                if (!isEditing) {
                                                    expandedReportId = if (isExpanded) null else report.id 
                                                }
                                            }
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp), 
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            // Top Row: Date & Status Badge
                                            Row(
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "📅 ${report.createdAt}",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = CharcoalText
                                                )
                                                
                                                val badgeColor = when (report.healthStatus) {
                                                    "Good" -> Color(0xFF2E7D32)
                                                    "Stable" -> DeepTeal
                                                    "Guarded" -> Color(0xFFE65100)
                                                    "Critical" -> Color(0xFFC62828)
                                                    else -> SlateGray
                                                }
                                                
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Box(
                                                        modifier = Modifier
                                                            .background(badgeColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(
                                                            text = (report.healthStatus ?: "Stable").uppercase(), 
                                                            fontSize = 9.sp, 
                                                            color = badgeColor, 
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                    Spacer(Modifier.width(6.dp))
                                                    Icon(
                                                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                        contentDescription = "Expand",
                                                        tint = SlateGray,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }

                                            // Doctor details
                                            Text(
                                                text = "Doctor: ${report.doctorName ?: "Specialist"} (${report.specialization ?: "Expert"})",
                                                fontSize = 11.sp,
                                                color = SlateGray,
                                                fontWeight = FontWeight.SemiBold
                                            )

                                            // Expanded Content
                                            if (isExpanded) {
                                                HorizontalDivider(color = BorderGray.copy(alpha = 0.5f), thickness = 0.5.dp)
                                                
                                                if (isEditing) {
                                                    // Inline editing mode
                                                    Text("Edit Health Status", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DeepTeal)
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        listOf("Good", "Stable", "Guarded", "Critical").forEach { status ->
                                                            val isSel = editStatus == status
                                                            val sColor = when (status) {
                                                                "Good" -> Color(0xFF2E7D32)
                                                                "Stable" -> DeepTeal
                                                                "Guarded" -> Color(0xFFE65100)
                                                                "Critical" -> Color(0xFFC62828)
                                                                else -> SlateGray
                                                            }
                                                            Box(
                                                                modifier = Modifier
                                                                    .background(if (isSel) sColor else SurfaceWhite, RoundedCornerShape(12.dp))
                                                                    .border(1.dp, if (isSel) Color.Transparent else BorderGray, RoundedCornerShape(12.dp))
                                                                    .clickable { editStatus = status }
                                                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                                            ) {
                                                                Text(
                                                                    text = status,
                                                                    fontSize = 10.sp,
                                                                    color = if (isSel) Color.White else SlateGray,
                                                                    fontWeight = FontWeight.Bold
                                                                )
                                                            }
                                                        }
                                                    }
                                                    
                                                    Text("Edit Prescriptions & Notes", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DeepTeal)
                                                    OutlinedTextField(
                                                        value = editNotes,
                                                        onValueChange = { editNotes = it },
                                                        modifier = Modifier.fillMaxWidth().height(100.dp),
                                                        colors = OutlinedTextFieldDefaults.colors(
                                                            focusedBorderColor = DeepTeal,
                                                            unfocusedBorderColor = BorderGray,
                                                            focusedTextColor = CharcoalText,
                                                            unfocusedTextColor = CharcoalText
                                                        ),
                                                        shape = RoundedCornerShape(8.dp)
                                                    )
                                                    
                                                    Row(
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                                                    ) {
                                                        Button(
                                                            onClick = {
                                                                onEditReport?.invoke(report.id ?: 0, editStatus, editNotes)
                                                                editingReportId = null
                                                                expandedReportId = null
                                                            },
                                                            modifier = Modifier.weight(1f),
                                                            shape = RoundedCornerShape(8.dp),
                                                            colors = ButtonDefaults.buttonColors(containerColor = DeepTeal)
                                                        ) {
                                                            Text("Save", fontSize = 12.sp, color = Color.White)
                                                        }
                                                        
                                                        OutlinedButton(
                                                            onClick = { editingReportId = null },
                                                            modifier = Modifier.weight(1f),
                                                            shape = RoundedCornerShape(8.dp),
                                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CharcoalText)
                                                        ) {
                                                            Text("Cancel", fontSize = 12.sp)
                                                        }
                                                    }
                                                } else {
                                                    // Display notes
                                                    if (!report.notes.isNullOrEmpty()) {
                                                        Text("Clinical Notes & Prescriptions:", fontSize = 11.sp, color = SlateGray, fontWeight = FontWeight.Bold)
                                                        Text(
                                                            text = report.notes ?: "",
                                                            fontSize = 13.sp,
                                                            color = CharcoalText,
                                                            lineHeight = 18.sp
                                                        )
                                                    }

                                                    // Display documents and images
                                                    val docs = report.documents
                                                    if (!docs.isNullOrEmpty()) {
                                                        Spacer(Modifier.height(4.dp))
                                                        Text("Uploaded Medical Scans:", fontSize = 11.sp, color = DeepTeal, fontWeight = FontWeight.Bold)
                                                        docs.forEach { doc ->
                                                            val path = doc.filePath ?: ""
                                                            if (path.isNotEmpty()) {
                                                                Card(
                                                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                                                                    shape = RoundedCornerShape(8.dp)
                                                                ) {
                                                                    Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                                            Text("📄", fontSize = 14.sp)
                                                                            Spacer(Modifier.width(6.dp))
                                                                            Text(
                                                                                text = path.substringAfterLast("_"),
                                                                                fontSize = 11.sp,
                                                                                color = CharcoalText,
                                                                                fontWeight = FontWeight.Medium,
                                                                                modifier = Modifier.weight(1f)
                                                                            )
                                                                        }
                                                                        if (path.endsWith(".png", true) || path.endsWith(".jpg", true) || path.endsWith(".jpeg", true) || doc.fileType?.contains("image", true) == true) {
                                                                            AsyncImage(
                                                                                model = "${com.simats.hospiq.network.ApiConfig.IMAGE_BASE_URL}uploads/reports/$path",
                                                                                contentDescription = "Medical Scan Preview",
                                                                                modifier = Modifier
                                                                                    .fillMaxWidth()
                                                                                    .height(140.dp)
                                                                                    .clip(RoundedCornerShape(6.dp)),
                                                                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                                                            )
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }

                                                    HorizontalDivider(color = BorderGray.copy(alpha = 0.5f), thickness = 0.5.dp, modifier = Modifier.padding(vertical = 4.dp))

                                                    // Action buttons
                                                    Row(
                                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        TextButton(
                                                            onClick = {
                                                                editStatus = report.healthStatus ?: "Stable"
                                                                editNotes = report.notes ?: ""
                                                                editingReportId = report.id
                                                            },
                                                            modifier = Modifier.weight(1f)
                                                        ) {
                                                            Icon(Icons.Default.Edit, "Edit", tint = DeepTeal, modifier = Modifier.size(16.dp))
                                                            Spacer(Modifier.width(4.dp))
                                                            Text("Edit Notes", fontSize = 12.sp, color = DeepTeal, fontWeight = FontWeight.Bold)
                                                        }
                                                        
                                                        TextButton(
                                                            onClick = {
                                                                onDeleteReport?.invoke(report.id ?: 0)
                                                                expandedReportId = null
                                                            },
                                                            modifier = Modifier.weight(1f)
                                                        ) {
                                                            Icon(Icons.Default.Delete, "Delete", tint = Color.Red, modifier = Modifier.size(16.dp))
                                                            Spacer(Modifier.width(4.dp))
                                                            Text("Delete", fontSize = 12.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    if (index < healthReports.size - 1) {
                                        Spacer(Modifier.height(8.dp))
                                    }
                                }
                            }
                        }
                    }

                    // 5. Collapsible "➕ Add New Health Report / Give Advice" Section
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Section trigger header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isNewReportFormVisible = !isNewReportFormVisible },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "➕ Add Patient Health Record & Advice", 
                                    fontSize = 12.sp, 
                                    fontWeight = FontWeight.Bold, 
                                    color = DeepTeal
                                )
                                Icon(
                                    imageVector = if (isNewReportFormVisible) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = "Expand",
                                    tint = DeepTeal,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            
                            if (isNewReportFormVisible) {
                                HorizontalDivider(color = BorderGray, thickness = 0.5.dp)
                                
                                Text("Patient Health Status", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CharcoalText)
                                
                                // Health Status Chips
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf("Good", "Stable", "Guarded", "Critical").forEach { status ->
                                        val isSelected = healthStatus == status
                                        val statusColor = when (status) {
                                            "Good" -> Color(0xFF2E7D32)
                                            "Stable" -> DeepTeal
                                            "Guarded" -> Color(0xFFE65100)
                                            "Critical" -> Color(0xFFC62828)
                                            else -> SlateGray
                                        }
                                        
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    if (isSelected) statusColor else SurfaceWhite,
                                                    RoundedCornerShape(18.dp)
                                                )
                                                .border(
                                                    1.dp,
                                                    if (isSelected) Color.Transparent else BorderGray,
                                                    RoundedCornerShape(18.dp)
                                                )
                                                .clickable { healthStatus = status }
                                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                        ) {
                                            Text(
                                                status,
                                                color = if (isSelected) Color.White else SlateGray,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }

                                Spacer(Modifier.height(4.dp))
                                Text("Prescriptions & Medical Advice", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DeepTeal)
                                Text("Provide prescription details, emergency guidance or general medical notes.", fontSize = 11.sp, color = SlateGray)
                                
                                OutlinedTextField(
                                    value = adviceText,
                                    onValueChange = { adviceText = it },
                                    placeholder = { Text("Enter prescriptions, dosage, or medical precautions...") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = DeepTeal,
                                        unfocusedBorderColor = BorderGray,
                                        focusedTextColor = CharcoalText,
                                        unfocusedTextColor = CharcoalText
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Spacer(Modifier.height(4.dp))
                                Text("Attach Reports / Documents", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DeepTeal)
                                
                                OutlinedButton(
                                    onClick = { fileLauncher.launch("image/*") },
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, DeepTeal)
                                ) {
                                    Icon(Icons.Default.AttachFile, null, tint = DeepTeal)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Select Scans / Prescriptions", color = DeepTeal, fontWeight = FontWeight.Bold)
                                }

                                // Show attached documents list
                                if (selectedDocs.isNotEmpty()) {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        selectedDocs.forEachIndexed { idx, uri ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(SoftTeal, RoundedCornerShape(8.dp))
                                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    "Attachment #${idx + 1} (${documentNames.getOrNull(idx) ?: "Image"})",
                                                    fontSize = 12.sp,
                                                    color = DeepTeal,
                                                    fontWeight = FontWeight.Medium
                                                )
                                                IconButton(
                                                    onClick = {
                                                        selectedDocs.removeAt(idx)
                                                        documentBytes.removeAt(idx)
                                                        documentNames.removeAt(idx)
                                                    },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(Icons.Default.Delete, null, tint = Color.Red, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                                
                                Spacer(Modifier.height(6.dp))
                                
                                Button(
                                    onClick = {
                                        isSubmitting = true
                                        if (onSubmitReport != null) {
                                            onSubmitReport(healthStatus, adviceText, documentBytes.toList(), documentNames.toList())
                                        } else {
                                            onSubmitAdvice(adviceText)
                                        }
                                    },
                                    enabled = adviceText.trim().isNotEmpty() && !isSubmitting,
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = DeepTeal)
                                ) {
                                    if (isSubmitting) {
                                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                                    } else {
                                        Text("Complete & Save Report", fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Footer Close Button
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CharcoalText)
                ) {
                    Text("Close Profile", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
