package com.simats.hospiq.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
fun SpecialtyChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (selected) DeepTeal else SurfaceWhite
    val textColor = if (selected) SurfaceWhite else SlateGray
    val shape = RoundedCornerShape(20.dp)
    Text(
        text = label,
        fontSize = 13.sp,
        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        color = textColor,
        modifier = modifier
            .background(bg, shape)
            .border(1.dp, if (selected) DeepTeal else BorderGray, shape)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 7.dp)
    )
}
