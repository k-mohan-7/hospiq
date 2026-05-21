package com.simats.hospiq.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ShimmerBox(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            tween(durationMillis = 1200, easing = LinearEasing),
            RepeatMode.Restart
        ),
        label = "shimmerAnim"
    )
    val shimmerBrush = Brush.linearGradient(
        colors = listOf(Color(0xFFE0E0E0), Color(0xFFF5F5F5), Color(0xFFE0E0E0)),
        start = Offset(translateAnim - 300f, 0f),
        end = Offset(translateAnim, 0f)
    )
    Box(modifier = modifier.background(shimmerBrush, RoundedCornerShape(8.dp)))
}

@Composable
fun ShimmerHospitalCard(modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        ShimmerBox(modifier = Modifier.fillMaxWidth().height(150.dp))
        Spacer(modifier = Modifier.height(8.dp))
        ShimmerBox(modifier = Modifier.fillMaxWidth(0.7f).height(18.dp))
        Spacer(modifier = Modifier.height(6.dp))
        ShimmerBox(modifier = Modifier.fillMaxWidth(0.5f).height(14.dp))
    }
}

@Composable
fun ShimmerDoctorCard(modifier: Modifier = Modifier) {
    Row(modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        ShimmerBox(modifier = Modifier.size(72.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            ShimmerBox(modifier = Modifier.width(160.dp).height(18.dp))
            Spacer(modifier = Modifier.height(6.dp))
            ShimmerBox(modifier = Modifier.width(100.dp).height(14.dp))
            Spacer(modifier = Modifier.height(6.dp))
            ShimmerBox(modifier = Modifier.width(80.dp).height(14.dp))
        }
    }
}
