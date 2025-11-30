package com.afsar.titipin.ui.components


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Warna Custom (Sesuaikan dengan tema kamu)
val PurpleIcon = Color(0xFF370061)     // Ungu Tua
val PurpleBg = Color(0xFFF2E7FE)       // Ungu Muda (Background Icon)
val RedText = Color(0xFFD32F2F)        // Merah (Untuk Keluar)
val RedBg = Color(0xFFFFEBEE)          // Merah Muda (Background Icon Keluar)
val TextBlack = Color(0xFF1E1E1E)      // Hitam Teks

@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDestructive: Boolean = false // Set TRUE jika ini tombol "Keluar"
) {
    // Tentukan warna berdasarkan tipe tombol (Biasa atau Destructive/Merah)
    val textColor = if (isDestructive) RedText else TextBlack
    val iconColor = if (isDestructive) RedText else PurpleIcon
    val iconBgColor = if (isDestructive) RedBg else PurpleBg

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp) // Jarak antar kartu
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp), // Sudut membulat
        colors = CardDefaults.cardColors(containerColor = Color.White), // Background putih
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp) // Sedikit bayangan biar timbul
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp), // Padding dalam kartu
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Kotak Ikon di Kiri
            Box(
                modifier = Modifier
                    .size(40.dp) // Ukuran kotak background ikon
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 2. Teks Menu
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = textColor,
                modifier = Modifier.weight(1f) // Dorong panah ke kanan mentok
            )

            // 3. Ikon Panah Kanan (Hanya muncul jika bukan tombol Keluar, opsional)
            // Biasanya tombol logout tidak ada panahnya, tapi kalau mau ada tinggal hapus 'if' nya
            if (!isDestructive) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// --- CONTOH PEMAKAIAN (PREVIEW) ---
@Preview(showBackground = true, backgroundColor = 0xFFF5F5F5)
@Composable
fun ProfileMenuPreview() {
    Column(modifier = Modifier.padding(16.dp)) {

        // Contoh Tombol Biasa
        ProfileMenuItem(
            icon = Icons.Default.History,
            text = "Riwayat Transaksi",
            onClick = {}
        )

        ProfileMenuItem(
            icon = Icons.Default.Group,
            text = "Circle Saya",
            onClick = {}
        )

        // Contoh Header Section
        Text(
            text = "LAINNYA",
            fontSize = 12.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 12.dp)
        )

        ProfileMenuItem(
            icon = Icons.Default.Help,
            text = "Pusat Bantuan",
            onClick = {}
        )

        // Contoh Tombol Merah (Logout)
        ProfileMenuItem(
            icon = Icons.AutoMirrored.Filled.ExitToApp,
            text = "Keluar",
            isDestructive = true, // <--- Ini bikin dia jadi merah
            onClick = {}
        )
    }
}