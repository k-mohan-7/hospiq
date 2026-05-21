package com.simats.hospiq.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.hospiq.ui.theme.*

@Composable
fun StatusBadge(status: String, modifier: Modifier = Modifier) {
    val (bgColor, textColor, label) = when (status.lowercase()) {
        "pending"    -> Triple(SoftTeal, DeepTeal, "Pending")
        "accepted"   -> Triple(MintWash, MintGreen, "Accepted")
        "rejected"   -> Triple(CoralWash, CoralOrange, "Rejected")
        "completed"  -> Triple(MintWash, MintGreen, "Completed")
        "rescheduled"-> Triple(androidx.compose.ui.graphics.Color(0xFFFFF8E1), AmberStar, "Rescheduled")
        "cancelled"  -> Triple(CoralWash, CoralOrange, "Cancelled")
        "available"  -> Triple(MintWash, MintGreen, "Available")
        "busy"       -> Triple(androidx.compose.ui.graphics.Color(0xFFFFF8E1), AmberStar, "Busy")
        "in_surgery" -> Triple(CoralWash, CoralOrange, "In Surgery")
        else         -> Triple(SoftTeal, DeepTeal, status.replaceFirstChar { it.uppercaseChar() })
    }

    Text(
        text = label,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = textColor,
        modifier = modifier
            .background(bgColor, RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}
