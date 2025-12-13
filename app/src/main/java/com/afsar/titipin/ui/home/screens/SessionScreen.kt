package com.afsar.titipin.ui.home.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.afsar.titipin.data.model.Session
import com.afsar.titipin.ui.home.viewmodel.SessionViewModel

@Composable
fun SessionScreen(
    onSessionItemClick: (Session) -> Unit,
    viewModel: SessionViewModel = hiltViewModel()
) {
    // --- PERBAIKAN: Load data saat screen dibuka ---
    LaunchedEffect(Unit) {
        viewModel.loadMySessions()
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("Titipanku (Riwayat)", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            if (viewModel.mySessions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Belum ada riwayat titipan.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(viewModel.mySessions) { session ->
                        SessionItemCard(session, onClick = { onSessionItemClick(session) })
                    }
                }
            }
        }
    }
}

@Composable
fun SessionItemCard(session: Session, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Status Visual
            val iconColor = if (session.status == "open") Color(0xFF00C853) else Color.Gray

            Icon(
                Icons.Default.ShoppingBag,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(session.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = if (session.status == "open") Color(0xFFE8F5E9) else Color(0xFFF5F5F5),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = if (session.status == "open") "Berlangsung" else "Selesai",
                            color = if (session.status == "open") Color(0xFF2E7D32) else Color.Gray,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}