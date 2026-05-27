package com.simats.hospiq.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.hospiq.ui.components.*
import com.simats.hospiq.ui.theme.*
import com.simats.hospiq.utils.DemoData
import com.simats.hospiq.viewmodels.HospitalUiState
import com.simats.hospiq.viewmodels.HospitalViewModel
import com.simats.hospiq.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    hospitalViewModel: HospitalViewModel,
    onHospitalClick: (Int) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToAppointments: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    var showFilter by remember { mutableStateOf(false) }
    var selectedSpecialty by remember { mutableStateOf("") }
    var selectedRating by remember { mutableStateOf("4.0+") }

    val recentSearches = listOf("Cardiology", "Apollo", "Dr. Priya", "Pediatrics")
    val specialties = listOf("Cardiology", "Neurology", "Pediatrics", "Dermatology", "General Surgery")
    val ratings = listOf("3+", "3.5+", "4.0+", "4.5+")

    val hospitalState by hospitalViewModel.listState.collectAsState()
    LaunchedEffect(Unit) { hospitalViewModel.loadHospitals() }
    val allHospitals = when (val s = hospitalState) {
        is HospitalUiState.Success -> s.topRatedHospitals
        else -> DemoData.hospitals
    }
    val results = allHospitals.filter { h ->
        (query.isEmpty() || h.name.contains(query, ignoreCase = true) || h.city.contains(query, ignoreCase = true)) &&
        (selectedSpecialty.isEmpty() || h.specialties.any { it.contains(selectedSpecialty, ignoreCase = true) })
    }

    Scaffold(
        containerColor = AppBackground,
        bottomBar = {
            BottomNavBar(
                currentRoute = Screen.Search.route,
                items = patientNavItems,
                onNavItemClick = { route ->
                    when (route) {
                        Screen.PatientHome.route -> onNavigateToHome()
                        Screen.PatientAppointments.route -> onNavigateToAppointments()
                        Screen.PatientNotifications.route -> onNavigateToNotifications()
                        Screen.PatientProfile.route -> onNavigateToProfile()
                    }
                }
            )
        },
        topBar = {
            Surface(shadowElevation = 2.dp) {
                Column(modifier = Modifier.background(SurfaceWhite).padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.res.painterResource(id = com.simats.hospiq.R.drawable.app_logo),
                            contentDescription = "Logo",
                            modifier = Modifier.size(34.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text("HospiQ", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DeepTeal, modifier = Modifier.weight(1f))
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(DeepTeal, androidx.compose.foundation.shape.CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("KS", color = SurfaceWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search hospitals, doctors...", color = DisabledGray) },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = SlateGray) },
                        trailingIcon = if (query.isNotEmpty()) {
                            { IconButton(onClick = { query = "" }) { Icon(Icons.Default.Close, null, tint = SlateGray) } }
                        } else null,
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DeepTeal,
                            unfocusedBorderColor = BorderGray,
                            focusedTextColor = CharcoalText,
                            unfocusedTextColor = CharcoalText
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Results count + filter button
            item {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${results.size} Results",
                        fontSize = 15.sp, fontWeight = FontWeight.Bold, color = CharcoalText,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedButton(
                        onClick = { showFilter = true },
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Tune, null, tint = DeepTeal, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Filter", color = CharcoalText, fontSize = 13.sp)
                    }
                }
            }

            // Recent searches
            if (query.isEmpty()) {
                item {
                    Text("Recent Searches", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = CharcoalText)
                    Spacer(Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(recentSearches) { tag ->
                            Text(
                                tag, fontSize = 13.sp, color = SlateGray,
                                modifier = Modifier
                                    .background(BorderGray.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // Hospital results
            if (results.isEmpty()) {
                item { EmptyState(title = "No results found", subtitle = "Try adjusting your search or filters") }
            } else {
                items(results) { hospital ->
                    HospitalCard(hospital = hospital, onClick = { onHospitalClick(hospital.id) })
                }
            }
        }
    }

    // Filter bottom sheet
    if (showFilter) {
        ModalBottomSheet(onDismissRequest = { showFilter = false }) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Filter Results", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = CharcoalText, modifier = Modifier.weight(1f))
                    TextButton(onClick = {
                        selectedSpecialty = ""
                        selectedRating = "4.0+"
                    }) {
                        Text("Reset All", color = CoralOrange, fontWeight = FontWeight.SemiBold)
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text("Specialties", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = CharcoalText)
                Spacer(Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(specialties) { spec ->
                        SpecialtyChip(
                            label = spec,
                            selected = selectedSpecialty == spec,
                            onClick = { selectedSpecialty = if (selectedSpecialty == spec) "" else spec }
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text("Minimum Rating", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = CharcoalText)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ratings.forEach { rating ->
                        val isSelected = selectedRating == rating
                        Box(
                            modifier = Modifier
                                .background(if (isSelected) DeepTeal else SurfaceWhite, RoundedCornerShape(10.dp))
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(rating, fontSize = 13.sp, color = if (isSelected) SurfaceWhite else CharcoalText, fontWeight = FontWeight.Medium)
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = { showFilter = false },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DeepTeal)
                ) {
                    Text("Apply Filters", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
