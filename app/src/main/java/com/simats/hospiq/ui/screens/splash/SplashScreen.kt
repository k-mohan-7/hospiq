package com.simats.hospiq.ui.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.hospiq.R
import com.simats.hospiq.ui.theme.DeepTeal
import com.simats.hospiq.ui.theme.CharcoalText
import com.simats.hospiq.ui.theme.SlateGray
import com.simats.hospiq.utils.SessionManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    sessionManager: SessionManager? = null,
    onNavigateToOnboarding: () -> Unit,
    onNavigateToPatientHome: () -> Unit = {},
    onNavigateToDoctorDashboard: () -> Unit = {}
) {
    // Premium entrance animations using physics-based Spring specs
    val scale = remember { Animatable(0.2f) }
    val alpha = remember { Animatable(0f) }
    
    val textAlpha = remember { Animatable(0f) }
    val textOffsetY = remember { Animatable(40f) } // Slide up slightly from bottom

    LaunchedEffect(Unit) {
        // 1. Logo entrance: scale and fade-in
        launch {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }
        launch {
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 700, easing = EaseOutQuad)
            )
        }

        // 2. Text entrance: delay slightly, then slide up and fade
        delay(400)
        launch {
            textAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 650, easing = EaseOutQuad)
            )
        }
        launch {
            textOffsetY.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }

        // 3. Full zoom and zoom-out before routing
        delay(1200)
        launch {
            scale.animateTo(
                targetValue = 1.2f,
                animationSpec = tween(durationMillis = 280, easing = EaseOutQuad)
            )
        }
        delay(160)
        launch {
            scale.animateTo(
                targetValue = 0.85f,
                animationSpec = tween(durationMillis = 320, easing = EaseInQuad)
            )
        }
        launch {
            alpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 320, easing = EaseInQuad)
            )
        }
        launch {
            textAlpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 220, easing = EaseInQuad)
            )
        }

        delay(340)
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
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "HospiQ Logo",
                modifier = Modifier
                    .size(150.dp)
                    .scale(scale.value)
                    .alpha(alpha.value)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Animated branding text and taglines
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .offset(y = textOffsetY.value.dp)
                    .alpha(textAlpha.value)
            ) {
                Text(
                    text = "HospiQ",
                    color = DeepTeal,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Your Health, Our Priority",
                    color = SlateGray,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}
