package com.example.titipin.ui.screen

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.titipin.R
import com.example.titipin.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SessionDetailScreen(
    onBackClick: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToTitipanku: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToShopping: () -> Unit = {}
) {
    // Dummy data
    var selectedTab by remember { mutableStateOf(0) }
    var timeRemaining by remember { mutableStateOf(14 * 60 + 32) } // 14:32 in seconds
    
    // Countdown timer
    LaunchedEffect(Unit) {
        while (timeRemaining > 0) {
            delay(1000)
            timeRemaining--
        }
        // Navigate to shopping when timer ends
        onNavigateToShopping()
    }
    
    val participants = remember {
        mutableStateListOf(
            ParticipantRequest(
                id = 1,
                name = "Budi Santoso",
                circleName = "Teman Kantor",
                orderItems = listOf(
                    OrderItem("Air Le mineral", 1),
                    OrderItem("Indomie Goreng", 2),
                    OrderItem("Teh Pucuk Harum", 1)
                ),
                notes = "Indomie nya rasa rendang",
                amount = "Rp 25.000",
                status = RequestStatus.PENDING,
                avatarRes = R.drawable.ic_profile1
            ),
            ParticipantRequest(
                id = 2,
                name = "Citra Lestari",
                circleName = "Mabar Valorant",
                orderItems = listOf(
                    OrderItem("Chocolatos Matcha Sachet", 3),
                    OrderItem("Nabati Richeese", 2)
                ),
                notes = "-",
                amount = "Rp 30.000",
                status = RequestStatus.PENDING,
                avatarRes = R.drawable.ic_profile2
            ),
            ParticipantRequest(
                id = 3,
                name = "Eko Wibowo",
                circleName = "Anak Fasilkom",
                orderItems = listOf(
                    OrderItem("Aqua Botol", 2),
                    OrderItem("Roti Aoka Coklat", 1)
                ),
                notes = "",
                amount = "Rp 15.000",
                status = RequestStatus.ACCEPTED,
                avatarRes = R.drawable.ic_profile1
            )
        )
    }
    
    val tabs = listOf("Menunggu", "Diterima", "Ditolak")
    
    Scaffold(
        containerColor = Color(0xFFF5F5F5),
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .statusBarsPadding()
                    .height(56.dp)
                    .padding(horizontal = 16.dp)
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Text(
                    text = "Detail Sesi",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        },
        bottomBar = {
            com.example.titipin.ui.screen.BottomNavBar(
                selectedTab = -1,
                onTabSelected = { tab: Int ->
                    when (tab) {
                        0 -> onNavigateToHome()
                        1 -> onNavigateToTitipanku()
                        2 -> onNavigateToProfile()
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
            }
            
            // Session Info Card
            item {
                SessionInfoCard(
                    title = "Alfamart Jakal",
                    location = "Alfamart Jakal KM 12.5",
                    participantCount = 5
                )
            }
            
            // Countdown Timer
            item {
                CountdownSection(
                    timeRemaining = timeRemaining,
                    onEndEarly = onNavigateToShopping
                )
            }
            
            // Tab Row
            item {
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = PrimaryColor,
                    edgePadding = 16.dp,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = PrimaryColor,
                            height = 3.dp
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        val count = when (index) {
                            0 -> participants.count { it.status == RequestStatus.PENDING }
                            1 -> participants.count { it.status == RequestStatus.ACCEPTED }
                            2 -> participants.count { it.status == RequestStatus.REJECTED }
                            else -> 0
                        }
                        
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = "$title ($count)",
                                    fontSize = 14.sp,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        )
                    }
                }
            }
            
            // Participant List
            val filteredParticipants = when (selectedTab) {
                0 -> participants.filter { it.status == RequestStatus.PENDING }
                1 -> participants.filter { it.status == RequestStatus.ACCEPTED }
                2 -> participants.filter { it.status == RequestStatus.REJECTED }
                else -> participants
            }
            
            items(filteredParticipants) { participant ->
                ParticipantCard(
                    participant = participant,
                    participants = participants,
                    onAccept = {
                        participants.remove(participant)
                    },
                    onReject = { /* Handle reject */ }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun SessionInfoCard(
    title: String,
    location: String,
    participantCount: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box {
            // Background Image
            Image(
                painter = painterResource(id = R.drawable.alfamart),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentScale = ContentScale.Crop
            )
            
            // Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    )
            )
            
            // Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.BottomStart)
            ) {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = location,
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "$participantCount Pesanan Masuk",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CountdownSection(
    timeRemaining: Int,
    onEndEarly: () -> Unit
) {
    val minutes = timeRemaining / 60
    val seconds = timeRemaining % 60
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Sesi ditutup dalam:",
                fontSize = 14.sp,
                color = TextSecondary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TimeDisplay(minutes, "Menit")
                Text(
                    text = ":",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryColor
                )
                TimeDisplay(seconds, "Detik")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onEndEarly,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryColor
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Selesaikan Sesi Lebih Awal", fontSize = 14.sp, color = Color.White)
            }
        }
    }
}

@Composable
fun TimeDisplay(value: Int, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = String.format("%02d", value),
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextSecondary
        )
    }
}

@Composable
fun ParticipantCard(
    participant: ParticipantRequest,
    participants: SnapshotStateList<ParticipantRequest>,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (participant.status) {
                RequestStatus.ACCEPTED -> Color(0xFFE8F5E9)
                RequestStatus.REJECTED -> Color(0xFFFFEBEE)
                else -> Color.White
            }
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Image(
                    painter = painterResource(id = participant.avatarRes),
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = participant.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = " ${participant.circleName}",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
                
                if (participant.status == RequestStatus.ACCEPTED) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Accepted",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Order Items (Pesanan)
            Text(
                text = "Pesanan:",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            participant.orderItems.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "• ${item.name}",
                        fontSize = 13.sp,
                        color = TextPrimary,
                        lineHeight = 18.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${item.quantity}x",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )
                }
            }
            
            // Notes (Catatan) - if any
            if (participant.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Catatan:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = participant.notes,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
            }
            
            // Action Buttons (only for pending)
            if (participant.status == RequestStatus.PENDING) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onReject,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFE53935)
                        ),
                        border = BorderStroke(1.dp, Color(0xFFE53935)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Tolak", fontSize = 14.sp)
                    }
                    
                    Button(
                        onClick = onAccept,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryColor
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Terima", fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

// Data classes
data class OrderItem(
    val name: String,
    val quantity: Int
)

data class ParticipantRequest(
    val id: Int,
    val name: String,
    val circleName: String,
    val orderItems: List<OrderItem>,
    val notes: String,
    val amount: String,
    val status: RequestStatus,
    val avatarRes: Int
)

enum class RequestStatus {
    PENDING, ACCEPTED, REJECTED
}
