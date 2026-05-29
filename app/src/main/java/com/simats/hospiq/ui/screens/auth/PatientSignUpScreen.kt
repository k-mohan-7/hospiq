package com.simats.hospiq.ui.screens.auth

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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

// ── Password strength helpers ────────────────────────────────────────────────
private fun passwordStrengthLevel(password: String): Int {
    if (password.isEmpty()) return 0
    var score = 0
    if (password.length >= 8) score++
    if (password.any { it.isUpperCase() }) score++
    if (password.any { it.isLowerCase() }) score++
    if (password.any { it.isDigit() }) score++
    if (password.any { !it.isLetterOrDigit() }) score++
    return score
}

private fun passwordStrengthLabel(level: Int) = when (level) {
    0 -> ""
    1 -> "Weak"
    2 -> "Fair"
    3 -> "Good"
    4 -> "Strong"
    else -> "Very Strong"
}

private fun passwordStrengthColor(level: Int) = when (level) {
    1 -> Color(0xFFD32F2F)
    2 -> Color(0xFFE65100)
    3 -> Color(0xFFF9A825)
    4 -> Color(0xFF388E3C)
    else -> Color(0xFF1B5E20)
}

@Composable
private fun PasswordStrengthBar(password: String) {
    val level = passwordStrengthLevel(password)
    if (password.isEmpty()) return
    val label = passwordStrengthLabel(level)
    val color = passwordStrengthColor(level)
    val animColor by animateColorAsState(targetValue = color, animationSpec = tween(300))
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(5) { i ->
                val barColor = if (i < level) animColor else BorderGray
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .background(barColor, RoundedCornerShape(2.dp))
                )
            }
        }
        if (label.isNotEmpty()) {
            Text(
                text = label,
                fontSize = 11.sp,
                color = animColor,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        // Requirements checklist
        val hasUpper = password.any { it.isUpperCase() }
        val hasLower = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecial = password.any { !it.isLetterOrDigit() }
        val hasLength = password.length >= 8
        Column(modifier = Modifier.padding(top = 4.dp)) {
            PasswordReqRow("At least 8 characters", hasLength)
            PasswordReqRow("Uppercase letter (A-Z)", hasUpper)
            PasswordReqRow("Lowercase letter (a-z)", hasLower)
            PasswordReqRow("Number (0-9)", hasDigit)
            PasswordReqRow("Special character (!@#\$...)", hasSpecial)
        }
    }
}

@Composable
private fun PasswordReqRow(text: String, met: Boolean) {
    val color = if (met) Color(0xFF388E3C) else SlateGray
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 1.dp)) {
        Text(if (met) "✓" else "○", fontSize = 10.sp, color = color, modifier = Modifier.width(14.dp))
        Text(text, fontSize = 10.sp, color = color)
    }
}

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
    var showPasswordStrength by remember { mutableStateOf(false) }

    // Local inline error states
    var emailError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    // Parse backend error message to show inline
    LaunchedEffect(authViewModel.errorMessage) {
        val err = authViewModel.errorMessage ?: return@LaunchedEffect
        val errLower = err.lowercase()
        when {
            errLower.contains("email") && (errLower.contains("exist") || errLower.contains("duplicate") || errLower.contains("already")) -> {
                emailError = "This email address is already registered"
            }
            errLower.contains("phone") && (errLower.contains("exist") || errLower.contains("duplicate") || errLower.contains("already")) -> {
                phoneError = "This phone number is already in use"
            }
        }
    }

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

                    // Global error banner
                    val globalError = authViewModel.errorMessage
                    if (globalError != null && !globalError.lowercase().let { it.contains("email") || it.contains("phone") }) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "⚠️ $globalError",
                            color = Color(0xFFD32F2F),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFFEBEE), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    @Composable
                    fun Field(
                        label: String, value: String, onValue: (String) -> Unit,
                        hint: String = "", keyboard: KeyboardType = KeyboardType.Text,
                        isPassword: Boolean = false, showPw: Boolean = false, onTogglePw: (() -> Unit)? = null,
                        errorMsg: String? = null
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
                            visualTransformation = if (isPassword && !showPw) PasswordVisualTransformation() else VisualTransformation.None,
                            trailingIcon = if (isPassword && onTogglePw != null) {
                                {
                                    IconButton(onClick = onTogglePw) {
                                        Icon(if (showPw) Icons.Default.Visibility else Icons.Default.VisibilityOff, null, tint = SlateGray)
                                    }
                                }
                            } else null,
                            isError = errorMsg != null,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (errorMsg != null) Color(0xFFD32F2F) else DeepTeal,
                                unfocusedBorderColor = if (errorMsg != null) Color(0xFFD32F2F) else BorderGray,
                                focusedTextColor = CharcoalText,
                                unfocusedTextColor = CharcoalText
                            )
                        )
                        if (errorMsg != null) {
                            Text(
                                text = "⚠ $errorMsg",
                                color = Color(0xFFD32F2F),
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 2.dp, start = 4.dp)
                            )
                        }
                        Spacer(Modifier.height(14.dp))
                    }

                    Field("Full Name", fullName, { fullName = it }, "John Doe")
                    Field(
                        "Email Address", email,
                        {
                            email = it
                            emailError = null // clear on change
                            authViewModel.clearError()
                        },
                        "name@example.com", KeyboardType.Email,
                        errorMsg = emailError
                    )
                    Field(
                        "Phone Number", phone,
                        {
                            phone = it
                            phoneError = null // clear on change
                            authViewModel.clearError()
                        },
                        "+1 (555) 000-0000", KeyboardType.Phone,
                        errorMsg = phoneError
                    )
                    Field(
                        "Password", password,
                        {
                            password = it
                            passwordError = null
                            showPasswordStrength = it.isNotEmpty()
                        },
                        "••••••••",
                        isPassword = true, showPw = showPassword, onTogglePw = { showPassword = !showPassword },
                        errorMsg = passwordError
                    )
                    if (showPasswordStrength) {
                        PasswordStrengthBar(password = password)
                        Spacer(Modifier.height(14.dp))
                    }
                    Field(
                        "Confirm Password", confirmPassword,
                        {
                            confirmPassword = it
                            confirmPasswordError = null
                        },
                        "••••••••",
                        isPassword = true, showPw = showConfirmPassword, onTogglePw = { showConfirmPassword = !showConfirmPassword },
                        errorMsg = confirmPasswordError
                    )

                    Button(
                        onClick = {
                            // Validate before calling API
                            var valid = true
                            if (fullName.isBlank() || email.isBlank() || phone.isBlank() || password.isBlank()) {
                                valid = false
                                passwordError = if (password.isBlank()) "Password is required" else null
                            }
                            if (passwordStrengthLevel(password) < 3) {
                                passwordError = "Password must include uppercase, lowercase, number & special character"
                                valid = false
                            }
                            if (password != confirmPassword) {
                                confirmPasswordError = "Passwords do not match"
                                valid = false
                            }
                            if (valid) {
                                authViewModel.registerPatient(fullName, email, phone, password) { token, userId, role, name, hospitalId, phoneVal, profilePhoto, doctorId ->
                                    onSignUpSuccess(token, userId, role, name, hospitalId, phoneVal, profilePhoto, doctorId)
                                }
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
