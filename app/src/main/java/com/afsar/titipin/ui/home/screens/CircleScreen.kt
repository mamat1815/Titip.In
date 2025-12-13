package com.afsar.titipin.ui.home.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.afsar.titipin.data.model.Circle
import com.afsar.titipin.ui.home.viewmodel.CircleViewModel
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun CircleScreen(
    onAddCircleClick: () -> Unit,
    onCircleItemClick: (Circle) -> Unit,
    viewModel: CircleViewModel = hiltViewModel()
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddCircleClick,
                containerColor = Color(0xFF008069), // Warna hijau khas WA/Titipin
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Buat Circle")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Header
            Text(
                text = "Circle Saya",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )

            // Handling State
            if (viewModel.isLoading && viewModel.myCircles.isEmpty()) {
                // Tampilkan Loading jika data belum ada sama sekali
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (viewModel.errorMessage != null && viewModel.myCircles.isEmpty()) {
                // Tampilkan Error
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = viewModel.errorMessage!!, color = Color.Red)
                }
            } else if (viewModel.myCircles.isEmpty()) {
                // Tampilkan Kosong
                EmptyCircleState()
            } else {
                // Tampilkan List
                LazyColumn {
                    items(viewModel.myCircles) { circle ->
                        CircleListItem(circle = circle, onClick = { onCircleItemClick(circle) })
                        Divider(color = Color.LightGray.copy(alpha = 0.2f), thickness = 0.5.dp) // Garis pemisah tipis
                    }
                }
            }
        }
    }
}

@Composable
fun CircleListItem(circle: Circle, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Avatar Circle (Kiri)
        Surface(
            modifier = Modifier.size(50.dp),
            shape = CircleShape,
            color = if (circle.isActiveSession) Color(0xFFE8F5E9) else Color(0xFFECEFF1) // Hijau muda jika aktif, abu jika tidak
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (circle.isActiveSession) Icons.Default.ShoppingBag else Icons.Default.Group,
                    contentDescription = null,
                    tint = if (circle.isActiveSession) Color(0xFF008069) else Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // 2. Info Tengah (Nama & Pesan Terakhir)
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = circle.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                // Timestamp di kanan atas (sebelah nama)
                if (circle.lastMessageTime != null) {
                    Text(
                        text = formatTime(circle.lastMessageTime),
                        fontSize = 11.sp,
                        color = if (circle.isActiveSession) Color(0xFF008069) else Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Pesan Terakhir / Status
            if (circle.isActiveSession) {
                Text(
                    text = "● Sesi Belanja Aktif!",
                    fontSize = 14.sp,
                    color = Color(0xFF008069), // Hijau
                    fontWeight = FontWeight.Medium
                )
            } else {
                Text(
                    text = circle.lastMessage.ifEmpty { "Belum ada percakapan" },
                    fontSize = 14.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun EmptyCircleState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Group,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Color.LightGray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Belum gabung circle manapun.", color = Color.Gray)
        Text("Buat circle baru untuk mulai jastip!", color = Color.Gray, fontSize = 12.sp)
    }
}

// Helper untuk format jam (Contoh output: 14:30)
fun formatTime(timestamp: Timestamp?): String {
    if (timestamp == null) return ""
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(timestamp.toDate())
}