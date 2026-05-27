package com.simats.hospiq.ui.screens.onboarding

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.hospiq.R
import com.simats.hospiq.ui.theme.*

data class OnboardingPageData(
    val imageRes: Int,
    val title: String,
    val subtitle: String
)

val onboardingPages = listOf(
    OnboardingPageData(
        imageRes = R.drawable.doctor_standing,
        title = "Verified Specialists",
        subtitle = "Access a network of top-rated healthcare providers near you."
    ),
    OnboardingPageData(
        imageRes = R.drawable.patient_booking,
        title = "Easy Booking",
        subtitle = "Schedule appointments in seconds from your phone."
    )
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onNavigateToPatientSignUp: () -> Unit,
    onNavigateToDoctorRegister: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })

    // A beautiful background gradient for a professional clinical/medical feel
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            Color.White,
            SoftTeal.copy(alpha = 0.4f),
            SoftTeal.copy(alpha = 0.9f)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(28.dp))

            // Fixed stationary Header: modern logo and slogan
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .shadow(4.dp, CircleShape)
                        .background(Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.app_logo),
                        contentDescription = "HospiQ Logo",
                        modifier = Modifier.size(30.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = "HospiQ",
                        color = DeepTeal,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Find care, book instantly.",
                        color = SlateGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Swipeable HorizontalPager carrying illustration and the detailed card
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { page ->
                val item = onboardingPages[page]
                
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Illustration Banner with modern shadow and rounded clip
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1.1f)
                            .shadow(6.dp, RoundedCornerShape(24.dp))
                            .background(Color.White, RoundedCornerShape(24.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = item.imageRes),
                            contentDescription = item.title,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(16.dp))
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    // Description card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.9f)
                            .shadow(4.dp, RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, BorderGray.copy(alpha = 0.6f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = item.title,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = CharcoalText,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = item.subtitle,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Normal,
                                color = SlateGray,
                                textAlign = TextAlign.Center,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }
            }

            // Animated Pager dots: custom pill indicator system
            Row(
                modifier = Modifier.padding(vertical = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(onboardingPages.size) { i ->
                    val isActive = i == pagerState.currentPage
                    val width = animateDpAsState(
                        targetValue = if (isActive) 24.dp else 8.dp,
                        animationSpec = tween(durationMillis = 300),
                        label = "dotWidth"
                    )
                    val alpha = animateFloatAsState(
                        targetValue = if (isActive) 1f else 0.4f,
                        animationSpec = tween(durationMillis = 300),
                        label = "dotAlpha"
                    )

                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(width.value)
                            .background(
                                color = if (isActive) DeepTeal else DeepTeal.copy(alpha = alpha.value),
                                shape = RoundedCornerShape(4.dp)
                            )
                    )
                }
            }

            // Action Buttons Panel
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Log In Action
                Button(
                    onClick = onNavigateToLogin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .shadow(4.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DeepTeal,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Log In",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        letterSpacing = 0.5.sp
                    )
                }

                // Sign Up Action
                OutlinedButton(
                    onClick = onNavigateToPatientSignUp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.5.dp, DeepTeal),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = DeepTeal
                    )
                ) {
                    Text(
                        text = "Create Account",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        letterSpacing = 0.5.sp
                    )
                }

                // Quick role navigation footer links
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Register as a ",
                        color = SlateGray,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    TextButton(
                        onClick = onNavigateToPatientSignUp,
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text(
                            text = "Patient",
                            color = CoralOrange,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = " or ",
                        color = SlateGray,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    TextButton(
                        onClick = onNavigateToDoctorRegister,
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text(
                            text = "Doctor",
                            color = CoralOrange,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
