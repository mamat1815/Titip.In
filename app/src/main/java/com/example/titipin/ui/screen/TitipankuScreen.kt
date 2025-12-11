package com.example.titipin.ui.screen

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.titipin.R
import com.example.titipin.ui.theme.*

@Composable
fun TitipankuScreen(
    onNavigateToHome: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    // ===== DUMMY DATA - EDIT DI SINI =====
    
    // Tab "Titip" - User titip barang ke orang lain
    val titipSessions = listOf(
        TitipSession(
            id = 1,
            title = "Titip Jajan di Kantin",
            recipientName = "Andi",
            location = "Kantin Gedung A",
            category = "Makanan/Minuman",
            iconRes = R.drawable.ic_makanan,
            status = TitipStatus.MENUNGGU_ACCEPT,
            amount = "Rp 25.000"
        ),
        TitipSession(
            id = 2,
            title = "Belanja Indomaret",
            recipientName = "Budi",
            location = "Indomaret Jakal",
            category = "Belanjaan",
            iconRes = R.drawable.ic_belanja,
            status = TitipStatus.DIPROSES,
            amount = "Rp 50.000"
        ),
        TitipSession(
            id = 3,
            title = "Beli Obat",
            recipientName = "Cici",
            location = "Apotek K24",
            category = "Obat-obatan",
            iconRes = R.drawable.ic_obat,
            status = TitipStatus.BAYAR_DAN_ANTAR,
            amount = "Rp 100.000"
        )
    )
    
    // Tab "Dititipi" - User buka jasa titipan
    val dititipiSessions = listOf(
        DititipiSession(
            id = 2,
            title = "Pizza Hut",
            location = "Pizza Hut Hartono Mall",
            category = "Makanan/Minuman",
            iconRes = R.drawable.ic_makanan,
            participantCount = 5,
            status = DititipiStatus.MENUNGGU_PESANAN,
            timeRemaining = "01:30:07"
        ),
        DititipiSession(
            id = 1,
            title = "Alfamart Jakal",
            location = "Alfamart Pogung",
            category = "Belanjaan",
            iconRes = R.drawable.ic_belanja,
            participantCount = 3,
            status = DititipiStatus.SEDANG_DIPROSES,
            timeRemaining = "00:45:12"
        )
    )
    
    // ===== END DUMMY DATA =====
    
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Titip", "Dititipi")
    
    Scaffold(
        containerColor = BgLight,
        bottomBar = {
            com.example.titipin.ui.screen.BottomNavBar(
                selectedTab = 1, // Titipanku selected
                onTabSelected = { index ->
                    when (index) {
                        0 -> onNavigateToHome()
                        1 -> {} // Already here
                        2 -> onNavigateToProfile()
                    }
                }
            )
        },
        topBar = {
            // Header - Thin & Card Style
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF5F5F5))
                    .padding(horizontal = 16.dp, vertical = 12.dp) // Reduced vertical padding
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Title
                        Text(
                            text = "Titipanku",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp, bottom = 4.dp) // Reduced bottom padding
                        )
                        
                        // Tab Row
                        TabRow(
                            selectedTabIndex = selectedTab,
                            containerColor = Color.Transparent,
                            contentColor = PrimaryColor,
                            indicator = { tabPositions ->
                                TabRowDefaults.Indicator(
                                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                    color = PrimaryColor,
                                    height = 2.dp // Thinner indicator
                                )
                            }
                        ) {
                            tabs.forEachIndexed { index, title ->
                                Tab(
                                    selected = selectedTab == index,
                                    onClick = { selectedTab = index },
                                    text = {
                                        Text(
                                            text = title,
                                            fontSize = 14.sp, // Reduced from 15sp
                                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                            modifier = Modifier.padding(vertical = 10.dp) // Reduced from 12dp
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            when (selectedTab) {
                0 -> {
                    // Tab Titip - User menitipkan barang
                    if (titipSessions.isEmpty()) {
                        EmptyState("Belum ada titipan")
                    } else {
                        titipSessions.forEach { session ->
                            TitipSessionCard(session)
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
                1 -> {
                    // Tab Dititipi - User buka jasa
                    if (dititipiSessions.isEmpty()) {
                        EmptyState("Belum ada jasa titipan")
                    } else {
                        dititipiSessions.forEach { session ->
                            DititipiSessionCard(session)
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

// Card untuk Tab "Titip"
@Composable
fun TitipSessionCard(session: TitipSession) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardLight),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFE1DAE7)),
        onClick = { /* Navigate to detail */ }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = session.iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp)
                )
            }
            
            // Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Title & Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = session.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // Recipient
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Dititipkan ke ${session.recipientName}",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
                
                // Location
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = session.location,
                        fontSize = 13.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // Amount & Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = session.amount,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryColor
                    )
                    
                    // Status Badge
                    StatusBadge(session.status.displayName, session.status.color)
                }
            }
        }
    }
}

// Card untuk Tab "Dititipi"
@Composable
fun DititipiSessionCard(session: DititipiSession) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardLight),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFE1DAE7)),
        onClick = { /* Navigate to detail */ }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = session.iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp)
                )
            }
            
            // Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Title
                Text(
                    text = session.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Location
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = session.location,
                        fontSize = 13.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // Participants & Time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.People,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${session.participantCount} penitip",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Timer,
                            contentDescription = null,
                            tint = if (session.status == DititipiStatus.SELESAI) TextSecondary else PrimaryColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = session.timeRemaining,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (session.status == DititipiStatus.SELESAI) TextSecondary else PrimaryColor
                        )
                    }
                }
                
                // Status Badge
                StatusBadge(session.status.displayName, session.status.color)
            }
        }
    }
}

@Composable
fun StatusBadge(text: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun EmptyState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 60.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.Inbox,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = message,
                fontSize = 16.sp,
                color = TextSecondary
            )
        }
    }
}

// ===== DATA CLASSES & ENUMS =====

// Status untuk "Titip" (user menitipkan)
enum class TitipStatus(val displayName: String, val color: Color) {
    MENUNGGU_ACCEPT("Menunggu Accept", Color(0xFFF59E0B)), // Orange
    DIPROSES("Diproses", Color(0xFF3B82F6)), // Blue
    BAYAR_DAN_ANTAR("Bayar & Antar", Color(0xFF10B981)) // Green
}

// Status untuk "Dititipi" (user buka jasa)
enum class DititipiStatus(val displayName: String, val color: Color) {
    MENUNGGU_PESANAN("Menunggu Pesanan", Color(0xFFF59E0B)), // Orange
    SEDANG_DIPROSES("Sedang Diproses", Color(0xFF3B82F6)), // Blue
    SELESAI("Selesai", Color(0xFF6B7280)) // Gray
}

// Data class untuk Tab "Titip"
data class TitipSession(
    val id: Int,
    val title: String,
    val recipientName: String, // Nama orang yang dititipi
    val location: String,
    val category: String,
    val iconRes: Int,
    val status: TitipStatus,
    val amount: String
)

// Data class untuk Tab "Dititipi"
data class DititipiSession(
    val id: Int,
    val title: String,
    val location: String,
    val category: String,
    val iconRes: Int,
    val participantCount: Int,
    val status: DititipiStatus,
    val timeRemaining: String
)
