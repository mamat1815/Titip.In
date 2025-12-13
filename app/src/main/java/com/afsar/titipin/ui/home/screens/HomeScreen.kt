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
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.afsar.titipin.data.model.Circle
import com.afsar.titipin.data.model.Session
import com.afsar.titipin.ui.components.CirclesImage
import com.afsar.titipin.ui.theme.BackgroundLight
import com.afsar.titipin.ui.theme.Primary
import com.afsar.titipin.ui.theme.jakartaFamily
import com.afsar.titipin.ui.home.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    onSessionClick: (String) -> Unit,
    onCreateSessionClick: () -> Unit,
    onCircleClick: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    // Ambil data langsung dari state ViewModel yang sudah diproses
    val currentUser = viewModel.currentUser
    val activeSession = viewModel.activeSession
    val myCircles = viewModel.myCircles
    val sessionHistory = viewModel.sessionHistory
    val isLoading = viewModel.isLoading

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateSessionClick,
                containerColor = Primary
            ) {
                Icon(Icons.Default.Add, null, tint = Color.White)
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
        ) {
            // 1. Header Profil
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CirclesImage(
                        imageUrl = currentUser?.photoUrl,
                        size = 40.dp,
                        modifier = Modifier.padding(end = 8.dp)
                    )

                    Spacer(Modifier.width(8.dp))

                    val name = currentUser?.name?.split(" ")?.firstOrNull() ?: "User"
                    Text(
                        text = "Halo, $name \uD83D\uDC4B",
                        fontFamily = jakartaFamily,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            item {
                Text(
                    text = "Beranda",
                    fontSize = 28.sp,
                    fontFamily = jakartaFamily,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 20.dp)
                )
            }

            // 2. Active Session Card
            item {
                // UI tidak perlu mikir filter lagi, tinggal cek null
                if (activeSession != null) {
                    ActiveSessionCard(
                        session = activeSession,
                        onClick = { onSessionClick(activeSession.id) }
                    )
                } else if (!isLoading) {
                    // Hanya tampilkan kosong jika loading selesai
                    EmptyActiveSessionCard()
                } else {
                    // Tampilkan Skeleton/Loading jika perlu, atau kosong sementara
                    Box(Modifier.height(100.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // 3. My Circles Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Circle Saya",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        fontFamily = jakartaFamily
                    )
                    Text(
                        "${myCircles.size} Circle",
                        color = Primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = jakartaFamily
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (myCircles.isEmpty() && !isLoading) {
                item {
                    EmptyCirclesCard()
                    Spacer(modifier = Modifier.height(24.dp))
                }
            } else {
                items(myCircles) { circle ->
                    CircleCard(
                        circle = circle,
                        onClick = { onCircleClick(circle.id) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // 4. History Section
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Riwayat Sesi",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        fontFamily = jakartaFamily
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // UI langsung pakai data history dari VM
            if (sessionHistory.isNotEmpty()) {
                items(sessionHistory) { session ->
                    HistorySessionCard(
                        session = session,
                        onClick = { onSessionClick(session.id) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            } else if (!isLoading) {
                item {
                    Text(
                        "Belum ada riwayat sesi.",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

// ... (Komponen Card lainnya ActiveSessionCard, CircleCard, HistorySessionCard tetap sama) ...

// ... (Sisa Composable Card di bawah ini biarkan sama seperti sebelumnya)

@Composable
fun ActiveSessionCard(session: Session, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sesi Aktif",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = jakartaFamily
                )
                Surface(
                    color = Color(0xFF4CAF50),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "BERJALAN",
                        fontSize = 10.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontFamily = jakartaFamily
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = session.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = jakartaFamily
            )

            Text(
                text = session.locationName.ifEmpty { "Lokasi belum diset" },
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp),
                fontFamily = jakartaFamily
            )
        }
    }
}

@Composable
fun EmptyActiveSessionCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Outlined.Timer,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tidak ada sesi aktif",
                fontSize = 14.sp,
                color = Color.Gray,
                fontFamily = jakartaFamily
            )
        }
    }
}

@Composable
fun CircleCard(circle: Circle, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF9C27B0), Color(0xFF673AB7))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Group,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    circle.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    fontFamily = jakartaFamily
                )
                Text(
                    "${circle.memberIds.size} anggota",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontFamily = jakartaFamily
                )
            }
        }
    }
}

@Composable
fun EmptyCirclesCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Group,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Belum ada circle",
                fontSize = 14.sp,
                color = Color.Gray,
                fontFamily = jakartaFamily
            )
            Text(
                text = "Buat circle pertamamu!",
                fontSize = 12.sp,
                color = Color.Gray,
                fontFamily = jakartaFamily
            )
        }
    }
}

@Composable
fun HistorySessionCard(session: Session, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Timer,
                    contentDescription = null,
                    tint = Color.Gray
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    session.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    fontFamily = jakartaFamily
                )
                Text(
                    session.locationName.ifEmpty { "Lokasi tidak diset" },
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontFamily = jakartaFamily
                )
            }

            Surface(
                color = Color(0xFFE0E0E0),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    "Selesai",
                    fontSize = 10.sp,
                    color = Color(0xFF424242),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontFamily = jakartaFamily
                )
            }
        }
    }
}