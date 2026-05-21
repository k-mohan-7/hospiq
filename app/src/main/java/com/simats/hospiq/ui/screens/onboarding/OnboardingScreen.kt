package com.simats.hospiq.ui.screens.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.hospiq.ui.theme.*

data class OnboardingPage(
    val emoji: String,
    val title: String,
    val subtitle: String
)

val onboardingPages = listOf(
    OnboardingPage(
        emoji = "👩‍⚕️",
        title = "Verified Specialists",
        subtitle = "Access a network of top-rated healthcare providers near you."
    ),
    OnboardingPage(
        emoji = "📅",
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepTeal)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(60.dp))

            // Logo + brand
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = com.simats.hospiq.R.drawable.app_logo),
                    contentDescription = "Logo",
                    modifier = Modifier.size(50.dp)
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = "HospiQ",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Find care, book instantly.",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp
            )

            Spacer(Modifier.height(32.dp))

            // Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 24.dp)
            ) { page ->
                val item = onboardingPages[page]
                Card(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = item.emoji, fontSize = 72.sp)
                        Spacer(Modifier.height(24.dp))
                        Text(
                            text = item.title,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = CharcoalText,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = item.subtitle,
                            fontSize = 15.sp,
                            color = SlateGray,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )
                    }
                }
            }

            // Page indicators
            Row(
                modifier = Modifier.padding(vertical = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(onboardingPages.size) { i ->
                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .width(if (i == pagerState.currentPage) 24.dp else 8.dp)
                            .background(
                                if (i == pagerState.currentPage) Color.White else Color.White.copy(alpha = 0.4f),
                                RoundedCornerShape(3.dp)
                            )
                    )
                }
            }

            // Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onNavigateToLogin,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    Text(text = "Log In", color = DeepTeal, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                OutlinedButton(
                    onClick = onNavigateToPatientSignUp,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color.White)
                ) {
                    Text(text = "Sign Up", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Register as a ", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                    TextButton(onClick = onNavigateToPatientSignUp, contentPadding = PaddingValues(0.dp)) {
                        Text("Patient", color = CoralOrange, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(" or ", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                    TextButton(onClick = onNavigateToDoctorRegister, contentPadding = PaddingValues(0.dp)) {
                        Text("Doctor", color = CoralOrange, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
