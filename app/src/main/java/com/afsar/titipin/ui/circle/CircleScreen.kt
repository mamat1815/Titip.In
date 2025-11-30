package com.afsar.titipin.ui.circle

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.afsar.titipin.data.model.Circle

@Composable
fun CircleScreen(
    onAddCircleClick: () -> Unit,
    onCircleItemClick: (Circle) -> Unit, // Callback ketika circle diklik
    viewModel: CircleViewModel = hiltViewModel()
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddCircleClick,
                containerColor = Color(0xFF008069),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Buat Circle")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text("Circle Saya", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            if (viewModel.myCircles.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Belum gabung circle manapun.", color = Color.Gray)
                }
            } else {
                LazyColumn {
                    items(viewModel.myCircles) { circle ->
                        CircleListItem(circle = circle, onClick = { onCircleItemClick(circle) })
                    }
                }
            }
        }
    }
}

@Composable
fun CircleListItem(circle: Circle, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Grup
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = Color(0xFFE0F2F1)
            ) {
                Icon(Icons.Default.Group, contentDescription = null, tint = Color(0xFF00695C), modifier = Modifier.padding(10.dp))
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(circle.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                if (circle.isActiveSession) {
                    Text("● Ada Sesi Aktif", fontSize = 12.sp, color = Color(0xFF008069), fontWeight = FontWeight.Medium)
                } else {
                    Text("Tidak ada sesi", fontSize = 12.sp, color = Color.Gray)
                }
            }
        }
    }
}