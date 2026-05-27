package com.simats.hospiq.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.hospiq.ui.theme.*
import com.simats.hospiq.utils.DemoData
import com.simats.hospiq.viewmodels.AuthViewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.simats.hospiq.ui.components.OSMMapPicker


@Composable
fun DoctorRegisterScreen(
    authViewModel: AuthViewModel,
    onRegistrationComplete: (token: String, userId: Int, role: String, name: String, hospitalId: Int?, phone: String?, profilePhoto: String?, doctorId: Int?) -> Unit,
    onBackClick: () -> Unit
) {
    var step by remember { mutableStateOf(1) }
    // Step 1
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    val context = LocalContext.current

    // Profile photo state
    var profilePhotoUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var profilePhotoBytes by remember { mutableStateOf<ByteArray?>(null) }
    var profilePhotoName by remember { mutableStateOf<String?>(null) }

    val profilePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let {
            profilePhotoUri = it
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                profilePhotoBytes = inputStream?.readBytes()
                profilePhotoName = "doctor_${System.currentTimeMillis()}.png"
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Step 2
    var specialization by remember { mutableStateOf("") }
    var licenseNumber by remember { mutableStateOf("") }
    var yearsExperience by remember { mutableStateOf("") }
    var languagesSpoken by remember { mutableStateOf("English") }
    // Step 3
    var hospitalSearch by remember { mutableStateOf("") }
    var selectedHospitalId by remember { mutableStateOf<Int?>(null) }
    
    // Hospital selection/creation state
    var isCreatingHospital by remember { mutableStateOf(false) }
    var hospitalName by remember { mutableStateOf("") }
    var hospitalAddress by remember { mutableStateOf("") }
    var hospitalCity by remember { mutableStateOf("") }
    var hospitalType by remember { mutableStateOf("Multispecialty") }
    var typeExpanded by remember { mutableStateOf(false) }
    var hospitalLatitude by remember { mutableStateOf<Double?>(null) }
    var hospitalLongitude by remember { mutableStateOf<Double?>(null) }
    var showMapPicker by remember { mutableStateOf(false) }

    var hospitalPhotoUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var hospitalPhotoBytes by remember { mutableStateOf<ByteArray?>(null) }
    var hospitalPhotoName by remember { mutableStateOf<String?>(null) }

    val hospitalPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let {
            hospitalPhotoUri = it
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                hospitalPhotoBytes = inputStream?.readBytes()
                hospitalPhotoName = "hospital_${System.currentTimeMillis()}.png"
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val hospitals = DemoData.hospitals.filter {
        hospitalSearch.isEmpty() || it.name.contains(hospitalSearch, ignoreCase = true)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
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
                Spacer(Modifier.width(10.dp))
                Text("HospiQ", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DeepTeal)
            }

            // Stepper
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("Personal", "Medical", "Hospital").forEachIndexed { index, label ->
                    val stepNum = index + 1
                    val isDone = step > stepNum
                    val isActive = step == stepNum

                    // Circle
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                if (isDone || isActive) DeepTeal else BorderGray,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isDone) {
                            Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        } else {
                            Text(
                                text = "$stepNum",
                                color = if (isActive) Color.White else SlateGray,
                                fontWeight = FontWeight.Bold, fontSize = 14.sp
                            )
                        }
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = label, fontSize = 11.sp,
                            color = if (isActive) DeepTeal else SlateGray,
                            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    if (index < 2) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                            color = if (step > stepNum) DeepTeal else BorderGray,
                            thickness = 2.dp
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Form card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    when (step) {
                        1 -> {
                            Text("Personal Information", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = CharcoalText)
                            Spacer(Modifier.height(4.dp))
                            Text("Tell us a bit about yourself to start your professional profile.", fontSize = 13.sp, color = SlateGray)
                            Spacer(Modifier.height(20.dp))
                            
                            // Mandatory profile image picker
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .align(Alignment.CenterHorizontally)
                                    .clip(CircleShape)
                                    .border(2.dp, DeepTeal, CircleShape)
                                    .background(SoftTeal.copy(alpha = 0.3f))
                                    .clickable { profilePhotoLauncher.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                if (profilePhotoUri != null) {
                                    AsyncImage(
                                        model = profilePhotoUri,
                                        contentDescription = "Profile Photo",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                } else {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("📷", fontSize = 24.sp)
                                        Spacer(Modifier.height(4.dp))
                                        Text("Add Photo", fontSize = 11.sp, color = DeepTeal, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            if (profilePhotoBytes == null) {
                                Text(
                                    "Profile photo is mandatory",
                                    color = Color.Red,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 4.dp)
                                )
                            } else {
                                Text(
                                    "Photo selected successfully",
                                    color = DeepTeal,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 4.dp)
                                )
                            }
                            Spacer(Modifier.height(20.dp))

                            RegField("Full Name", fullName, { fullName = it }, "Dr. Jane Smith")
                            RegField("Email Address", email, { email = it }, "jane.smith@medical.com", KeyboardType.Email)
                            RegField("Phone Number", phone, { phone = it }, "+1 (555) 000-0000", KeyboardType.Phone)
                            RegField("Password", password, { password = it }, "••••••••", KeyboardType.Password)
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    if (fullName.isBlank() || email.isBlank() || phone.isBlank() || password.isBlank()) {
                                        android.widget.Toast.makeText(context, "Please fill all fields", android.widget.Toast.LENGTH_SHORT).show()
                                    } else if (profilePhotoBytes == null) {
                                        android.widget.Toast.makeText(context, "Doctor profile picture is mandatory", android.widget.Toast.LENGTH_SHORT).show()
                                    } else {
                                        step = 2
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DeepTeal)
                            ) {
                                Text("Next: Medical Credentials", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                        2 -> {
                            Text("Medical Credentials", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = CharcoalText)
                            Spacer(Modifier.height(4.dp))
                            Text("Your professional qualifications and expertise.", fontSize = 13.sp, color = SlateGray)
                            Spacer(Modifier.height(20.dp))
                            RegField("Specialization", specialization, { specialization = it }, "e.g. Cardiologist")
                            RegField("License Number", licenseNumber, { licenseNumber = it }, "MCI-12345")
                            RegField("Years of Experience", yearsExperience, { yearsExperience = it }, "e.g. 10", KeyboardType.Number)
                            RegField("Languages Spoken (comma-separated)", languagesSpoken, { languagesSpoken = it }, "e.g. English, Spanish, Hindi")
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedButton(
                                    onClick = { step = 1 },
                                    modifier = Modifier.weight(1f).height(52.dp),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Text("Back", color = CharcoalText)
                                }
                                Button(
                                    onClick = { step = 3 },
                                    modifier = Modifier.weight(1f).height(52.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = DeepTeal)
                                ) {
                                    Text("Next: Hospital", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        3 -> {
                            Text("Workplace Setup", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = CharcoalText)
                            Spacer(Modifier.height(4.dp))
                            Text("Final step! Connect with your hospital or register your own practice.", fontSize = 13.sp, color = SlateGray)
                            Spacer(Modifier.height(16.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .background(SoftTeal.copy(alpha = 0.6f), RoundedCornerShape(22.dp))
                                    .padding(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(if (!isCreatingHospital) DeepTeal else Color.Transparent)
                                        .clickable { isCreatingHospital = false },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "Join Existing",
                                        color = if (!isCreatingHospital) Color.White else DeepTeal,
                                        fontWeight = FontWeight.Bold, fontSize = 13.sp
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(if (isCreatingHospital) DeepTeal else Color.Transparent)
                                        .clickable { isCreatingHospital = true },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "Create New",
                                        color = if (isCreatingHospital) Color.White else DeepTeal,
                                        fontWeight = FontWeight.Bold, fontSize = 13.sp
                                    )
                                }
                            }
                            Spacer(Modifier.height(16.dp))

                            if (!isCreatingHospital) {
                                OutlinedTextField(
                                    value = hospitalSearch,
                                    onValueChange = { hospitalSearch = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = { Text("Search by name or city...", color = DisabledGray) },
                                    leadingIcon = { Icon(Icons.Default.Search, null, tint = SlateGray, modifier = Modifier.size(20.dp)) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = DeepTeal,
                                        unfocusedBorderColor = BorderGray,
                                        focusedTextColor = CharcoalText,
                                        unfocusedTextColor = CharcoalText
                                    ),
                                    singleLine = true
                                )
                                Spacer(Modifier.height(12.dp))

                                hospitals.forEach { hospital ->
                                    val isSelected = selectedHospitalId == hospital.id
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp)
                                            .border(
                                                1.dp,
                                                if (isSelected) DeepTeal else BorderGray,
                                                RoundedCornerShape(12.dp)
                                            )
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelected) SoftTeal else SurfaceWhite)
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("🏥", fontSize = 28.sp)
                                        Spacer(Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(hospital.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = CharcoalText)
                                            Text(hospital.address, fontSize = 12.sp, color = SlateGray, maxLines = 1)
                                        }
                                        OutlinedButton(
                                            onClick = { selectedHospitalId = hospital.id },
                                            shape = RoundedCornerShape(20.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, DeepTeal),
                                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                            modifier = Modifier.height(34.dp)
                                        ) {
                                            Text(if (isSelected) "✓ Selected" else "Select", color = DeepTeal, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            } else {
                                // Create New Hospital Form
                                Text("Create a New Hospital", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = CharcoalText)
                                Spacer(Modifier.height(10.dp))

                                RegField("Hospital Name", hospitalName, { hospitalName = it }, "City Care Hospital")
                                RegField("Address", hospitalAddress, { hospitalAddress = it }, "123 Health Ave")
                                RegField("City", hospitalCity, { hospitalCity = it }, "Chennai")

                                Text("Hospital Type", fontSize = 13.sp, color = SlateGray, fontWeight = FontWeight.Medium)
                                Spacer(Modifier.height(6.dp))
                                Box {
                                    OutlinedTextField(
                                        value = hospitalType,
                                        onValueChange = {},
                                        readOnly = true,
                                        modifier = Modifier.fillMaxWidth().clickable { typeExpanded = true },
                                        trailingIcon = {
                                            IconButton(onClick = { typeExpanded = true }) {
                                                Icon(Icons.Default.ArrowDropDown, null, tint = SlateGray)
                                            }
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = DeepTeal,
                                            unfocusedBorderColor = BorderGray,
                                            focusedTextColor = CharcoalText,
                                            unfocusedTextColor = CharcoalText
                                        )
                                    )
                                    DropdownMenu(
                                        expanded = typeExpanded,
                                        onDismissRequest = { typeExpanded = false },
                                        modifier = Modifier.fillMaxWidth(0.8f).background(SurfaceWhite)
                                    ) {
                                        listOf("Government", "Private", "Clinic", "Multispecialty").forEach { type ->
                                            DropdownMenuItem(
                                                text = { Text(type, color = CharcoalText) },
                                                onClick = {
                                                    hospitalType = type
                                                    typeExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                                Spacer(Modifier.height(16.dp))

                                Text("Hospital Photo", fontSize = 13.sp, color = SlateGray, fontWeight = FontWeight.Medium)
                                Spacer(Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp)
                                        .border(1.dp, BorderGray, RoundedCornerShape(12.dp))
                                        .background(SoftTeal.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                        .clickable { hospitalPhotoLauncher.launch("image/*") },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (hospitalPhotoUri != null) {
                                        AsyncImage(
                                            model = hospitalPhotoUri,
                                            contentDescription = "Hospital Photo",
                                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                        )
                                    } else {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("🏥", fontSize = 24.sp)
                                            Spacer(Modifier.height(4.dp))
                                            Text("Select Hospital Photo", fontSize = 12.sp, color = DeepTeal, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                                if (hospitalPhotoBytes == null) {
                                    Text(
                                        "Hospital photo is mandatory when creating new hospital",
                                        color = Color.Red,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                                Spacer(Modifier.height(16.dp))

                                Text("Hospital GPS Location", fontSize = 13.sp, color = SlateGray, fontWeight = FontWeight.Medium)
                                Spacer(Modifier.height(6.dp))
                                OutlinedButton(
                                    onClick = { showMapPicker = true },
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, DeepTeal)
                                ) {
                                    Text(
                                        if (hospitalLatitude != null && hospitalLongitude != null)
                                            "📍 Location Selected: ${"%.4f".format(hospitalLatitude)}, ${"%.4f".format(hospitalLongitude)}"
                                        else "🗺️ Select Location on Map",
                                        color = DeepTeal,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                if (hospitalLatitude == null) {
                                    Text(
                                        "Hospital location coordinates are required",
                                        color = Color.Red,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                                Spacer(Modifier.height(16.dp))

                                if (showMapPicker) {
                                    OSMMapPicker(
                                        context = context,
                                        initialLat = 13.0827,
                                        initialLng = 80.2707,
                                        onLocationSelected = { lat, lng ->
                                            hospitalLatitude = lat
                                            hospitalLongitude = lng
                                        },
                                        onDismiss = { showMapPicker = false }
                                    )
                                }
                            }

                            Spacer(Modifier.height(12.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = SoftTeal
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text("ℹ️", fontSize = 16.sp)
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "Hospital verification usually takes 24-48 hours. Your profile will be visible once approved.",
                                        fontSize = 12.sp, color = DeepTeal, lineHeight = 18.sp
                                    )
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedButton(
                                    onClick = { step = 2 },
                                    modifier = Modifier.weight(1f).height(52.dp),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Text("Back", color = CharcoalText)
                                }
                                Button(
                                    onClick = {
                                        if (!isCreatingHospital && selectedHospitalId == null) {
                                            android.widget.Toast.makeText(context, "Please select a hospital", android.widget.Toast.LENGTH_SHORT).show()
                                        } else if (isCreatingHospital && (hospitalName.isBlank() || hospitalAddress.isBlank() || hospitalCity.isBlank() || hospitalPhotoBytes == null || hospitalLatitude == null || hospitalLongitude == null)) {
                                            android.widget.Toast.makeText(context, "Please fill all hospital fields, select coordinates, and upload photo", android.widget.Toast.LENGTH_SHORT).show()
                                        } else {
                                            authViewModel.registerDoctor(
                                                fullName = fullName,
                                                email = email,
                                                phone = phone,
                                                password = password,
                                                specialization = specialization,
                                                licenseNumber = licenseNumber,
                                                yearsExp = yearsExperience.toIntOrNull() ?: 0,
                                                bio = "${specialization} specialist",
                                                languages = languagesSpoken,
                                                hospitalId = selectedHospitalId ?: 0,
                                                createHospital = isCreatingHospital,
                                                hospitalName = hospitalName,
                                                hospitalAddress = hospitalAddress,
                                                hospitalCity = hospitalCity,
                                                hospitalType = hospitalType,
                                                hospitalLatitude = hospitalLatitude,
                                                hospitalLongitude = hospitalLongitude,
                                                profilePhotoBytes = profilePhotoBytes,
                                                profilePhotoName = profilePhotoName,
                                                hospitalPhotoBytes = hospitalPhotoBytes,
                                                hospitalPhotoName = hospitalPhotoName
                                            ) { token, userId, role, name, hospitalId, phoneVal, profilePhoto, doctorId ->
                                                onRegistrationComplete(token, userId, role, name, hospitalId, phoneVal, profilePhoto, doctorId)
                                            }
                                        }
                                    },
                                    enabled = !authViewModel.isLoading,
                                    modifier = Modifier.weight(1f).height(52.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = DeepTeal)
                                ) {
                                    if (authViewModel.isLoading) {
                                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                                    } else {
                                        Text("Complete Registration", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun RegField(
    label: String, value: String, onValue: (String) -> Unit,
    hint: String = "", keyboard: KeyboardType = KeyboardType.Text
) {
    Text(label, fontSize = 13.sp, color = SlateGray, fontWeight = FontWeight.Medium)
    Spacer(Modifier.height(6.dp))
    OutlinedTextField(
        value = value, onValueChange = onValue,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(hint, color = DisabledGray) },
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboard),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = DeepTeal,
            unfocusedBorderColor = BorderGray,
            focusedTextColor = CharcoalText,
            unfocusedTextColor = CharcoalText
        )
    )
    Spacer(Modifier.height(14.dp))
}
