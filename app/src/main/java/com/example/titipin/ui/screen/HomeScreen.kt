package com.example.titipin.ui.screen

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.titipin.R
import com.example.titipin.ui.theme.*

@Composable
fun HomeScreen(
    onNavigateToCreate: () -> Unit,
    onNavigateToTitipanku: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToRequest: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToShopping: () -> Unit = {},
    onNavigateToInProgress: () -> Unit = {}
) {
    // ===== DUMMY DATA - BISA DIUBAH-UBAH DI SINI =====
    
    // Dummy Transactions - Edit sesuka hati
    val transactions = listOf(
        DummyTransaction(
            title = "Indomaret Jakal",
            date = "05 Oktober 2023",
            amount = "Rp 60.000",
            iconRes = R.drawable.ic_belanja
        ),
        DummyTransaction(
            title = "28 Coffee",
            date = "04 Oktober 2023",
            amount = "Rp 22.500",
            iconRes = R.drawable.ic_makanan
        ),
        DummyTransaction(
            title = "Apotek K24",
            date = "02 Oktober 2023",
            amount = "Rp 163.000",
            iconRes = R.drawable.ic_obat
        ),
        DummyTransaction(
            title = "Pizza Hut",
            date = "01 Oktober 2023",
            amount = "Rp 25.000",
            iconRes = R.drawable.ic_makanan
        )
    )
    
    // Dummy Weekly Stats - Edit persentase sesuka hati
    val weeklyStats = listOf(60, 40, 80, 25, 95, 0, 0) // Percentage 0-100
    
    // Profile image dari drawable
    val profileImageRes = R.drawable.ic_profile1
    
    // Participant avatars untuk sesi card
    val participantAvatars = listOf(
        R.drawable.ic_profile1,
        R.drawable.ic_profile2,
        R.drawable.ic_profile1
    )
    
    // ===== END DUMMY DATA =====

    Scaffold(
        containerColor = BgLight,
        bottomBar = { 
            BottomNavBar(
                selectedTab = 0, // Home selected
                onTabSelected = { index ->
                    when (index) {
                        0 -> {} // Already on home
                        1 -> onNavigateToTitipanku()
                        2 -> onNavigateToProfile()
                    }
                }
            ) 
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreate,
                containerColor = PrimaryColor,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Buat Sesi",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Header dengan Profile & Notifikasi
            HeaderSection(
                profileImageRes = profileImageRes,
                onNotificationClick = onNavigateToNotifications
            )

            Spacer(modifier = Modifier.height(16.dp))
            
            // Notification "Open Jastip"
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                OpenJastipNotification(onTitipClick = onNavigateToRequest)
            }

            // Main Content
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = paddingValues.calculateBottomPadding())
            ) {
                // Title
                Text(
                    text = "Hai Syauqi, Mau titip apa hari ini?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(top = 24.dp, bottom = 12.dp)
                )

                // Sesi Dititipin Saat Ini Card
                ActiveSessionCard(
                    title = "Sesi Dititipin Saat Ini",
                    placeName = "Alfamart Jakal",
                    timer = "01:30:07",
                    description = "Aku pakai motor jadi gabisa titip yang berat-berat ya!",
                    iconRes = R.drawable.ic_belanja,
                    participantAvatars = participantAvatars,
                    showAvatars = true,
                    onClick = onNavigateToShopping
                )

                Spacer(modifier = Modifier.height(16.dp))
                
                // Sesi Titip Saat Ini Card (NEW)
                ActiveSessionCard(
                    title = "Sesi Titip Saat Ini",
                    placeName = "Indomaret Point",
                    timer = "00:45:20",
                    description = "Menunggu barang dibelikan oleh Budi...",
                    iconRes = R.drawable.ic_makanan,
                    participantAvatars = emptyList(),
                    showAvatars = false,
                    onClick = onNavigateToInProgress
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Pendapatan Mingguan Chart
                WeeklyIncomeCard(weeklyStats = weeklyStats)

                // Riwayat Section
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Riwayat Titipan",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    TextButton(onClick = onNavigateToHistory) {
                        Text(
                            text = "Lihat Semua",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryColor
                        )
                    }
                }

                // Transaction Items
                transactions.forEach { transaction ->
                    TransactionHistoryItem(
                        title = transaction.title,
                        date = transaction.date,
                        amount = transaction.amount,
                        iconRes = transaction.iconRes
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

// Dummy Transaction Data Class
data class DummyTransaction(
    val title: String,
    val date: String,
    val amount: String,
    val iconRes: Int
)

@Composable
fun HeaderSection(
    profileImageRes: Int,
    onNotificationClick: () -> Unit
) {
    // Outer container with background
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
    ) {
        // Card yang mengelilingi content
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Profile & Greeting
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Profile Picture
                    Image(
                        painter = painterResource(id = profileImageRes),
                        contentDescription = "Profile",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color(0xFFE5E7EB), CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    // Greeting Text
                    Column {
                        Text(
                            text = "Selamat Datang Kembali",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Normal
                        )
                        Text(
                            text = "Syauqi Fikri",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }

                // Notification Button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF5F5F5))
                        .clickable { onNotificationClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.notification),
                        contentDescription = "Notifikasi",
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ActiveSessionCard(
    title: String,
    placeName: String,
    timer: String,
    description: String,
    iconRes: Int,
    participantAvatars: List<Int>,
    showAvatars: Boolean = true,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardLight),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFE1DAE7))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with Timer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.timer),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = timer,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Session Title with Icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Category Icon
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = "Category",
                    modifier = Modifier.size(24.dp)
                )
                
                Text(
                    text = placeName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Session Description
            Text(
                text = description,
                fontSize = 14.sp,
                color = TextSecondary,
                lineHeight = 20.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Footer with Avatars & Link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Participant Avatars - only if showAvatars is true
                if (showAvatars) {
                    Row {
                        participantAvatars.forEachIndexed { index, avatarRes ->
                            Image(
                                painter = painterResource(id = avatarRes),
                                contentDescription = null,
                                modifier = Modifier
                                    .offset(x = (index * -8).dp)
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, Color.White, CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Box(
                            modifier = Modifier
                                .offset(x = (participantAvatars.size * -8).dp)
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE5E7EB))
                                .border(2.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+2",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp)) // Spacer to keep alignment if needed
                }

                // Detail Link
                TextButton(onClick = onClick) {
                    Text(
                        text = "Lihat Detail",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryColor
                    )
                }
            }
        }
    }
}

@Composable
fun WeeklyIncomeCard(weeklyStats: List<Int>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardLight),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFE1DAE7))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Pendapatan Mingguan",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Bar Chart
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.Bottom
            ) {
                val days = listOf("S", "S", "R", "K", "J", "S", "M")

                weeklyStats.forEachIndexed { index, percentage ->
                    WeeklyBarItem(
                        day = days[index],
                        percentage = percentage,
                        isActive = percentage > 0
                    )
                }
            }
        }
    }
}

@Composable
fun WeeklyBarItem(day: String, percentage: Int, isActive: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxHeight()
    ) {
        // Bar container
        Box(
            modifier = Modifier
                .width(32.dp) // Increased from 16dp
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (isActive) AccentColor.copy(alpha = 0.2f)
                    else PrimaryColor.copy(alpha = 0.2f)
                ),
            contentAlignment = Alignment.BottomCenter
        ) {
            // Filled bar
            if (isActive) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(percentage / 100f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(AccentColor)
                )
            }
        }

        // Day label
        Text(
            text = day,
            fontSize = 12.sp,
            color = TextSecondary
        )
    }
}

@Composable
fun TransactionHistoryItem(
    title: String,
    date: String,
    amount: String,
    iconRes: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardLight),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFE1DAE7))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false) // Prevent overflow
            ) {
                // Icon Container
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Transparent), // No background, langsung ke card
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = iconRes),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp) // Slightly bigger since no background
                    )
                }

                // Text Info - constrained width
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = date,
                        fontSize = 14.sp,
                        color = TextSecondary,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Amount - fixed alignment
            Text(
                text = amount,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                textAlign = TextAlign.End,
                modifier = Modifier.widthIn(min = 80.dp) // Minimum width untuk alignment
            )
        }
    }
}

@Composable
fun BottomNavBar(
    selectedTab: Int = 0,
    onTabSelected: (Int) -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp),
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Beranda
            NavIconWithIndicator(
                iconRes = R.drawable.home,
                selected = selectedTab == 0,
                onClick = { onTabSelected(0) }
            )
            
            // Titipanku
            NavIconWithIndicator(
                iconRes = R.drawable.titipanku,
                selected = selectedTab == 1,
                onClick = { onTabSelected(1) }
            )
            
            // Profile
            NavIconWithIndicator(
                iconRes = R.drawable.profile,
                selected = selectedTab == 2,
                onClick = { onTabSelected(2) }
            )
        }
    }
}

@Composable
private fun NavIconWithIndicator(
    iconRes: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(80.dp)
            .fillMaxHeight()
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(26.dp),
            tint = if (selected) PrimaryColor else Color(0xFF9E9E9E)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Indicator line
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(3.dp)
                .background(
                    color = if (selected) PrimaryColor else Color.Transparent,
                    shape = RoundedCornerShape(2.dp)
                )
        )
    }
}

@Composable
fun OpenJastipNotification(onTitipClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)), // Light Orange
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFFFB74D))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.NotificationsActive, 
                    contentDescription = null,
                    tint = Color(0xFFF57C00)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Temanmu sedang buka Jastip!",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE65100)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Fulan (Indomaret Jakal)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Lagi di Indomaret nih, ada yang mau titip?",
                fontSize = 14.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onTitipClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF57C00)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Titip Sekarang")
            }
        }
    }
}
