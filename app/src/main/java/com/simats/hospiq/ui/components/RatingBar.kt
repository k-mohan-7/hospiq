package com.simats.hospiq.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.automirrored.filled.StarHalf
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.simats.hospiq.ui.theme.AmberStar
import com.simats.hospiq.ui.theme.BorderGray
import kotlin.math.floor

@Composable
fun RatingBar(
    rating: Float,
    maxStars: Int = 5,
    size: Dp = 16.dp,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val fullStars = floor(rating).toInt()
        val hasHalf = (rating - fullStars) >= 0.5f
        val emptyStars = maxStars - fullStars - if (hasHalf) 1 else 0

        repeat(fullStars) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = AmberStar,
                modifier = Modifier.size(size)
            )
        }
        if (hasHalf) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.StarHalf,
                contentDescription = null,
                tint = AmberStar,
                modifier = Modifier.size(size)
            )
        }
        repeat(emptyStars) {
            Icon(
                imageVector = Icons.Default.StarBorder,
                contentDescription = null,
                tint = BorderGray,
                modifier = Modifier.size(size)
            )
        }
    }
}
