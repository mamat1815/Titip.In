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
import com.afsar.titipin.R
import com.afsar.titipin.data.model.Category
import com.afsar.titipin.data.model.SessionType
import com.afsar.titipin.data.model.TitipSession
import com.afsar.titipin.ui.theme.*

@Composable
fun SessionCard(session: TitipSession) {
    val categoryIcon = when (session.category) {
        Category.FOOD -> R.drawable.ic_makanan
        Category.MEDICINE -> R.drawable.ic_obat
        Category.SHOPPING -> R.drawable.ic_belanja
    }

    // Logic Warna & Teks Status
    val isWaitingAccept = session.status.equals("Menunggu diterima", ignoreCase = true)

    fun getStatusAttributes(status: String): Pair<Color, Color> {
        return when (status.lowercase()) {
            "menunggu diterima", "menunggu permintaan" -> Pair(Color(0xFFE8F5E9), Color(0xFF2E7D32)) // Hijau
            "diproses", "belanja" -> Pair(Color(0xFFE0F2FE), Color(0xFF0EA5E9)) // Biru
            "diantar", "antar" -> Pair(Color(0xFFFFF7E0), Color(0xFFEF6C00)) // Orange
            else -> Pair(Color(0xFFF5F5F5), Color.Gray)
        }
    }
    val (statusBg, statusText) = getStatusAttributes(session.status)

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

            // --- HEADER ---
            Box(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(end = 16.dp).padding(top = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Icon
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(65.dp).clip(CircleShape).background(OrangePrimary.copy(alpha = 0.50f))
                    ) {
                        Image(painter = painterResource(id = categoryIcon), contentDescription = null, modifier = Modifier.size(40.dp))
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Text Content
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = session.merchantName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextPrimary,
                            maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))

                        // KHUSUS SAAT MENUNGGU DITERIMA: TAMPILKAN NAMA HOST
                        if (isWaitingAccept && session.type == SessionType.GUEST) {
                            Text(
                                text = session.hostName, // "Host: Daviar"
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                        }

                        // Deskripsi (List Barang atau Pesan)
                        Text(
                            text = session.description,
                            fontSize = 12.sp,
                            color = TextSecondary,
                            maxLines = 2, overflow = TextOverflow.Ellipsis,
                            lineHeight = 16.sp
                        )
                    }
                }

                // Badge Status
                Row(modifier = Modifier.align(Alignment.TopEnd).offset(y = (-6).dp, x = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(8.dp), color = statusBg) {
                        Text(text = session.status, color = statusText, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Surface(shape = RoundedCornerShape(8.dp), color = Color.White, border = BorderStroke(1.dp, Color(0xFFEEEEEE))) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)) {
                            val typeIcon = if (session.type == SessionType.GUEST) R.drawable.ic_titip else R.drawable.ic_sesi
                            val typeText = if (session.type == SessionType.GUEST) "Titip" else "Sesi"
                            Image(painter = painterResource(id = typeIcon), contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = typeText, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                    }
                }
            }

            // --- FOOTER (AVATAR & TIMER) ---

            if (!isWaitingAccept) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Avatar Stack
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        session.avatars.take(3).forEachIndexed { index, drawableId ->
                            Image(
                                painter = painterResource(id = drawableId),
                                contentDescription = null,
                                modifier = Modifier.offset(x = (index * -10).dp).size(32.dp).clip(CircleShape).border(1.dp, Color.White, CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                        if (session.memberCount > 0) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.offset(x = (-20).dp).size(32.dp).clip(CircleShape).background(Color(0xFFFDE68A)).border(1.dp, Color.White, CircleShape)) {
                                Text("+${session.memberCount}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                        }
                    }

                    // Timer
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(painter = painterResource(id = R.drawable.ic_timer), contentDescription = "Timer", modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(session.timer, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = "Detail", tint = OrangePrimary, modifier = Modifier.size(24.dp))
                    }
                }
            }
        }
    }
}