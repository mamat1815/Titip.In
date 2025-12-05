package com.afsar.titipin.ui.circle

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.afsar.titipin.data.model.Circle
import com.afsar.titipin.data.model.JastipSession
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CircleDetailScreen(
    onBackClick: () -> Unit,
    onSessionClick: (JastipSession) -> Unit,
    viewModel: CircleDetailViewModel = hiltViewModel()
) {
    val circle = viewModel.circleState
    val activeSession = viewModel.activeSession
    val sessionHistory = viewModel.sessionHistory // Sesi masa lalu
    val timerString = viewModel.remainingTime
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(circle?.name ?: "Loading...", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        if (circle == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            Column(modifier = Modifier.padding(padding)) {

                ActiveSessionCard(
                    session = activeSession,
                    timerValue = timerString,
                    onClick = {
                        if (activeSession != null) {
                            onSessionClick(activeSession)
                        }
                    }
                )

                var selectedTabIndex by remember { mutableIntStateOf(0) }
                val tabs = listOf("Anggota (${circle.members.size})", "Riwayat Sesi")

                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.White,
                    contentColor = Color(0xFF370061),
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = Color(0xFF370061)
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Text(
                                    title,
                                    fontWeight = if(selectedTabIndex==index) FontWeight.Bold else FontWeight.Normal,
                                    color = if(selectedTabIndex==index) Color(0xFF370061) else Color.Gray
                                )
                            }
                        )
                    }
                }

                // 3. CONTENT
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF5F7FA))
                        .padding(top = 8.dp)
                ) {
                    if (selectedTabIndex == 0) {
                        MembersListContent(circle)
                    } else {
                        SessionHistoryContent(sessionHistory)
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveSessionCard(session: JastipSession?, timerValue: String, onClick: () -> Unit) {
    val isSessionExists = session != null
    val isOpen = session?.status == "open"

    val bgBrush = if (isSessionExists && isOpen) {
        Brush.horizontalGradient(listOf(Color(0xFF370061), Color(0xFF5E35B1))) // Ungu Premium
    } else {
        Brush.horizontalGradient(listOf(Color(0xFF757575), Color(0xFF9E9E9E))) // Abu-abu Gelap
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable(enabled = isSessionExists) {
                onClick()
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(
            modifier = Modifier
                .background(bgBrush)
                .padding(20.dp)
                .fillMaxWidth()
        ) {
            if (isSessionExists) {
                // --- TAMPILAN JIKA ADA SESI (OPEN / CLOSED) ---
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Badge Status
                        val badgeColor = if (isOpen) Color(0xFF00E676) else Color(0xFFE0E0E0) // Hijau / Abu Terang
                        val badgeText = if (isOpen) "OPEN" else "SELESAI"
                        val badgeTextColor = if (isOpen) Color.Black else Color.Gray

                        Surface(color = badgeColor, shape = RoundedCornerShape(4.dp)) {
                            Text(
                                text = badgeText,
                                color = badgeTextColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        
                        // Timer Icon & Text
                        Icon(Icons.Default.Timer, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isOpen) {
                                if (timerValue.isNotEmpty()) "$timerValue lagi" else "Selesai"
                            } else {
                                "Berakhir"
                            },
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = session.title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color.White.copy(0.8f), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Oleh: ${session.creatorName}",
                            color = Color.White.copy(0.9f),
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.White.copy(0.8f), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = session.locationName,
                            color = Color.White.copy(0.9f),
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Text Action
                    val actionText = if (isOpen) "Ketuk untuk melihat detail & pesan >" else "Lihat rincian sesi >"
                    Text(actionText, fontSize = 12.sp, color = Color(0xFFFFD54F), fontWeight = FontWeight.Bold)
                }
            } else {
                // --- TAMPILAN KOSONG (BELUM ADA SESI SAMA SEKALI) ---
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ShoppingBag,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Belum Ada Sesi",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Jadilah yang pertama membuka jastip!",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MembersListContent(circle: Circle) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(circle.members) { user ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(0.5.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(user.photoUrl.ifEmpty { null })
                            .crossfade(true).build(),
                        contentDescription = null,
                        placeholder = rememberVectorPainter(Icons.Default.Person),
                        error = rememberVectorPainter(Icons.Default.Person),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.LightGray)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(user.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("@${user.username}", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun SessionHistoryContent(sessions: List<JastipSession>) {
    if (sessions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Belum ada riwayat sesi.", color = Color.Gray)
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(sessions) { session ->
                SessionHistoryItem(session)
            }
        }
    }
}

@Composable
fun SessionHistoryItem(session: JastipSession) {
    val dateFormat = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
    val dateString = try { dateFormat.format(session.createdAt.toDate()) } catch (e: Exception) { "" }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(session.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Selesai", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(session.creatorName, fontSize = 12.sp, color = Color.Gray)

                Spacer(modifier = Modifier.width(12.dp))
                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(dateString, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}