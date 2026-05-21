package com.simats.hospiq.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.hospiq.ui.theme.*
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import com.simats.hospiq.viewmodels.AuthViewModel

@Composable
fun PatientLoginScreen(
    authViewModel: AuthViewModel,
    onLoginSuccess: (token: String, userId: Int, role: String, name: String) -> Unit,
    onBackClick: () -> Unit,
    onNavigateToSignUp: (() -> Unit)? = null,
    onNavigateToDoctorRegister: (() -> Unit)? = null
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf("patient") }
    val snackbarHostState = remember { SnackbarHostState() }

    // Show error in snackbar
    LaunchedEffect(authViewModel.errorMessage) {
        authViewModel.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            authViewModel.clearError()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppBackground)
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(60.dp))
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color.White, androidx.compose.foundation.shape.CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.simats.hospiq.R.drawable.app_logo),
                        contentDescription = "Logo",
                        modifier = Modifier.size(50.dp)
                    )
                }
                Spacer(Modifier.height(14.dp))
                Text("HospiQ", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = DeepTeal)
                Text(
                    "Your gateway to professional clinical healthcare services.",
                    fontSize = 13.sp, color = SlateGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 40.dp)
                )
                Spacer(Modifier.height(28.dp))

                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        // Segmented 2-way Pill Toggle Button
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .background(SoftTeal.copy(alpha = 0.6f), RoundedCornerShape(23.dp))
                                .padding(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (selectedRole == "patient") DeepTeal else Color.Transparent)
                                    .clickable { selectedRole = "patient" },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Patient",
                                    color = if (selectedRole == "patient") Color.White else DeepTeal,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (selectedRole == "doctor") DeepTeal else Color.Transparent)
                                    .clickable { selectedRole = "doctor" },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Doctor",
                                    color = if (selectedRole == "doctor") Color.White else DeepTeal,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                        Spacer(Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (selectedRole == "doctor") "Doctor Sign In" else "Sign In",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = CharcoalText
                            )
                            TextButton(onClick = {
                                if (selectedRole == "doctor") {
                                    onNavigateToDoctorRegister?.invoke()
                                } else {
                                    onNavigateToSignUp?.invoke()
                                }
                            }) {
                                Text("Sign Up Instead", color = DeepTeal, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            }
                        }
                        Text(
                            text = if (selectedRole == "doctor") "Access your clinical dashboard." else "Welcome back, please login.",
                            fontSize = 14.sp,
                            color = SlateGray
                        )
                        Spacer(Modifier.height(20.dp))

                        Text("Email Address", fontSize = 13.sp, color = SlateGray, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value = email, onValueChange = { email = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("name@example.com", color = DisabledGray) },
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DeepTeal, unfocusedBorderColor = BorderGray
                            )
                        )
                        Spacer(Modifier.height(14.dp))
                        Text("Password", fontSize = 13.sp, color = SlateGray, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value = password, onValueChange = { password = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("••••••••", color = DisabledGray) },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showPassword = !showPassword }) {
                                    Icon(
                                        imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null, tint = SlateGray
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DeepTeal, unfocusedBorderColor = BorderGray
                            )
                        )
                        Spacer(Modifier.height(24.dp))
                        Button(
                            onClick = {
                                authViewModel.login(email, password) { token, userId, role, name ->
                                    onLoginSuccess(token, userId, role, name)
                                }
                            },
                            enabled = !authViewModel.isLoading,
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DeepTeal)
                        ) {
                            if (authViewModel.isLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                            } else {
                                Text("Sign In", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                            }
                        }
                        Spacer(Modifier.height(24.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                            Text("Don't have an account? ", color = SlateGray, fontSize = 13.sp)
                            TextButton(
                                onClick = {
                                    if (selectedRole == "doctor") {
                                        onNavigateToDoctorRegister?.invoke()
                                    } else {
                                        onNavigateToSignUp?.invoke()
                                    }
                                },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Sign up", color = DeepTeal, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(40.dp))
            }
        }
    }
}
