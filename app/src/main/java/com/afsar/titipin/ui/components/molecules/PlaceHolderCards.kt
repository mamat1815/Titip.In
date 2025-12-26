package com.afsar.titipin.ui.components.molecules

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.afsar.titipin.R
import com.afsar.titipin.ui.theme.*

// 1. Placeholder kalau tidak ada sesi aktif
@Composable
fun EmptySessionCard() {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(24.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Gunakan gambar placeholder lokal (bisa icon belanja atau placeholder yang kemarin)
            Image(
                painter = painterResource(R.drawable.ic_nosession),
                contentDescription = null,
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Kamu sedang tidak titip/dititipi saat ini",
                fontSize = 14.sp,
                color = TextSecondary,
                lineHeight = 20.sp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// 2. Placeholder kalau riwayat kosong (Hanya teks di tengah)
@Composable
fun EmptyHistoryText() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 180.dp), // Jarak dari judul biar agak ke bawah
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Kamu belum pernah menitip.",
            color = TextSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}