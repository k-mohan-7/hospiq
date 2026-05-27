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
import com.simats.hospiq.viewmodels.AuthViewModel

@Composable
fun PatientSignUpScreen(
    authViewModel: AuthViewModel,
    onSignUpSuccess: (token: String, userId: Int, role: String, name: String, hospitalId: Int?, phone: String?, profilePhoto: String?, doctorId: Int?) -> Unit,
    onBackClick: () -> Unit,
    onNavigateToLogin: (() -> Unit)? = null
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(52.dp))
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

            Spacer(Modifier.height(24.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Account Creation for the Patient", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = CharcoalText)
                        TextButton(onClick = { onNavigateToLogin?.invoke() }) {
                            Text("Log In Instead", color = DeepTeal, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        }
                    }
                    Text("Join our healthcare community today.", fontSize = 13.sp, color = SlateGray)
                    Spacer(Modifier.height(20.dp))

                    @Composable
                    fun Field(label: String, value: String, onValue: (String) -> Unit,
                              hint: String = "", keyboard: KeyboardType = KeyboardType.Text,
                              isPassword: Boolean = false, showPw: Boolean = false, onTogglePw: (() -> Unit)? = null) {
                        Text(label, fontSize = 13.sp, color = SlateGray, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value = value, onValueChange = onValue,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text(hint, color = DisabledGray) },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = keyboard),
                            visualTransformation = if (isPassword && !showPw) PasswordVisualTransformation() else VisualTransformation.None,
                            trailingIcon = if (isPassword && onTogglePw != null) {
                                {
                                    IconButton(onClick = onTogglePw) {
                                        Icon(if (showPw) Icons.Default.Visibility else Icons.Default.VisibilityOff, null, tint = SlateGray)
                                    }
                                }
                            } else null,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DeepTeal,
                                unfocusedBorderColor = BorderGray,
                                focusedTextColor = CharcoalText,
                                unfocusedTextColor = CharcoalText
                            )
                        )
                        Spacer(Modifier.height(14.dp))
                    }

                    Field("Full Name", fullName, { fullName = it }, "John Doe")
                    Field("Email Address", email, { email = it }, "name@example.com", KeyboardType.Email)
                    Field("Phone Number", phone, { phone = it }, "+1 (555) 000-0000", KeyboardType.Phone)
                    Field("Password", password, { password = it }, "••••••••",
                        isPassword = true, showPw = showPassword, onTogglePw = { showPassword = !showPassword })
                    Field("Confirm Password", confirmPassword, { confirmPassword = it }, "••••••••",
                        isPassword = true, showPw = showConfirmPassword, onTogglePw = { showConfirmPassword = !showConfirmPassword })

                    Button(
                        onClick = {
                            authViewModel.registerPatient(fullName, email, phone, password) { token, userId, role, name, hospitalId, phoneVal, profilePhoto, doctorId ->
                                onSignUpSuccess(token, userId, role, name, hospitalId, phoneVal, profilePhoto, doctorId)
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
                            Text("Sign Up", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("Already have an account? ", color = SlateGray, fontSize = 13.sp)
                        TextButton(onClick = { onNavigateToLogin?.invoke() }, contentPadding = PaddingValues(0.dp)) {
                            Text("Log in", color = DeepTeal, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}
