package com.afsar.titipin.ui.components.molecules

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.afsar.titipin.R
import com.afsar.titipin.data.model.Category
import com.afsar.titipin.data.model.Session
import com.afsar.titipin.ui.theme.*
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

@Composable
fun SessionCard(
    session: Session,
    avatarUrls: List<String>,
    currentUserId: String? = null,
    customDescription: String? = null
) {
    val isCreator = session.creatorId == currentUserId
    val typeText = if (isCreator) "Sesi" else "Titip"
    val typeIcon = if (isCreator) R.drawable.ic_sesi else R.drawable.ic_titip

    val categoryIcon = when (session.category) {
        Category.FOOD -> R.drawable.ic_makanan
        Category.MEDICINE -> R.drawable.ic_obat
        Category.SHOPPING -> R.drawable.ic_belanja
    }

    fun getStatusAttributes(status: String): Triple<Color, Color, String> {
        return when (status.lowercase()) {
            "open" -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), "Menunggu")
            "shopping" -> Triple(Color(0xFFE0F2FE), Color(0xFF0EA5E9), "Belanja")
            "settling" -> Triple(Color(0xFFFFF7E0), Color(0xFFEF6C00), "Pembagian")
            "completed" -> Triple(Color(0xFFEEEEEE), Color.Gray, "Selesai")
            "cancelled" -> Triple(Color(0xFFFFEBEE), Color.Red, "Batal")
            else -> Triple(Color(0xFFF5F5F5), Color.Gray, status)
        }
    }
    val (statusBg, statusText, statusLabel) = getStatusAttributes(session.status)

    var timeDisplay by remember { mutableStateOf("00:00:00") }

    LaunchedEffect(session.createdAt, session.durationMinutes, session.status) {
        if (session.status.lowercase() != "open") {
            timeDisplay = if (session.status.lowercase() == "completed") "Selesai" else "-"
            return@LaunchedEffect
        }

        if (session.createdAt != null) {
            val createdTime = session.createdAt!!.toDate().time
            val durationMillis = TimeUnit.MINUTES.toMillis(session.durationMinutes.toLong())
            val endTime = createdTime + durationMillis

            while (true) {
                val now = System.currentTimeMillis()
                val diff = endTime - now

                if (diff > 0) {
                    val hours = TimeUnit.MILLISECONDS.toHours(diff)
                    val minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60
                    val seconds = TimeUnit.MILLISECONDS.toSeconds(diff) % 60

                    timeDisplay = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                    delay(1000L)
                } else {
                    timeDisplay = "Waktu Habis"
                    break
                }
            }
        } else {
            timeDisplay = "-"
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .border(1.dp, OrangePrimary, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(OrangePrimary.copy(alpha = 0.1f))
                ) {
                    Image(
                        painter = painterResource(id = categoryIcon),
                        contentDescription = null,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 2.dp)
                ) {
                    Text(
                        text = session.locationName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Host: ${session.creatorName}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = customDescription ?: session.description.ifEmpty { session.title },
                        fontSize = 12.sp,
                        color = TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))


                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = statusBg
                    ) {
                        Text(
                            text = statusLabel,
                            color = statusText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Image(painter = painterResource(id = typeIcon), contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = typeText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    avatarUrls.take(3).forEachIndexed { index, url ->
                        AsyncImage(
                            model = url,
                            contentDescription = "Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .offset(x = (index * -10).dp)
                                .size(32.dp)
                                .clip(CircleShape)
                                .border(1.dp, Color.White, CircleShape)
                                .background(Color.Gray)
                        )
                    }

                    if (session.currentTitipCount > avatarUrls.size) {
                        val remaining = session.currentTitipCount - avatarUrls.size
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .offset(x = (avatarUrls.size * -10).dp)
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFDE68A))
                                .border(1.dp, Color.White, CircleShape)
                        ) {
                            Text(
                                text = "+$remaining",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_timer),
                        contentDescription = "Timer",
                        modifier = Modifier.size(18.dp),
                        tint = OrangePrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = timeDisplay,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Detail",
                        tint = OrangePrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
