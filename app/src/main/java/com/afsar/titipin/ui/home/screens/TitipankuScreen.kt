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
import com.afsar.titipin.data.model.JastipSession
import com.afsar.titipin.ui.home.viewmodel.TitipankuViewModel

@Composable
fun TitipankuScreen(
    onSessionClick: (JastipSession) -> Unit,
    viewModel: TitipankuViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.loadMySessions()
    }

    Scaffold { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text("Titipanku", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(viewModel.mySessions) { session ->
                    SessionItemCard(session, onClick = { onSessionClick(session) })
                }
            }
        }
    }
}

@Composable
fun SessionItemCard(session: JastipSession, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = Color(0xFF370061))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(session.title, fontWeight = FontWeight.Bold)
                Text(
                    if (session.status == "open") "Sedang Berlangsung" else "Selesai",
                    color = if (session.status == "open") Color(0xFF00C853) else Color.Gray,
                    fontSize = 12.sp
                )
            }
        }
    }
}