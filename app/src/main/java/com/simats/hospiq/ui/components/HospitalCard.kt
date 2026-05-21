package com.simats.hospiq.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.simats.hospiq.network.ApiConfig
import com.simats.hospiq.network.models.Hospital
import com.simats.hospiq.ui.theme.*

@Composable
fun HospitalCard(
    hospital: Hospital,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Column {
            // Hospital image with dynamic Coil loading
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(SoftTeal)
            ) {
                if (!hospital.photo.isNullOrEmpty()) {
                    AsyncImage(
                        model = "${ApiConfig.IMAGE_BASE_URL}uploads/hospitals/${hospital.photo}",
                        contentDescription = hospital.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = "🏥",
                        fontSize = 48.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                // Bookmark
                IconButton(
                    onClick = {},
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(32.dp)
                        .background(Color.White.copy(alpha = 0.9f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = DeepTeal,
                        modifier = Modifier.size(18.dp)
                    )
                }
                // Rating badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .background(Color.White.copy(alpha = 0.95f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = AmberStar,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(Modifier.width(3.dp))
                        Text(
                            text = hospital.avgRating.toString(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CharcoalText
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = hospital.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = CharcoalText,
                    maxLines = 1
                )
                if (hospital.specialties.isNotEmpty()) {
                    Text(
                        text = hospital.specialties.take(2).joinToString(" • "),
                        fontSize = 12.sp,
                        color = DeepTeal,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .background(SoftTeal, RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = SlateGray,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "${hospital.distance}km • ${hospital.openingHours}",
                        fontSize = 12.sp,
                        color = SlateGray
                    )
                }
            }
        }
    }
}
