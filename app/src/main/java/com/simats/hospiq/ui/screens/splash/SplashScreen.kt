package com.simats.hospiq.ui.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.hospiq.ui.theme.DeepTeal
import com.simats.hospiq.utils.SessionManager
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    sessionManager: SessionManager? = null,
    onNavigateToOnboarding: () -> Unit,
    onNavigateToPatientHome: () -> Unit = {},
    onNavigateToDoctorDashboard: () -> Unit = {}
) {
    // Animate alpha and scale for logo
    val alpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 800),
        label = "splashAlpha"
    )

    LaunchedEffect(Unit) {
        delay(2200)
        if (sessionManager?.isLoggedIn() == true) {
            when (sessionManager.getRole()) {
                "doctor" -> onNavigateToDoctorDashboard()
                else -> onNavigateToPatientHome()
            }
        } else {
            onNavigateToOnboarding()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepTeal),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.alpha(alpha)
        ) {
            // Logo circle
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .background(Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = com.simats.hospiq.R.drawable.app_logo),
                    contentDescription = "Logo",
                    modifier = Modifier.size(60.dp)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "HospiQ",
                color = Color.White,
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Your Health, Our Priority",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 16.sp,
                letterSpacing = 0.5.sp
            )
        }
    }
}
