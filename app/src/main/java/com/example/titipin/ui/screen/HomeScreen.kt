package com.example.titipin.ui.screen

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
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
fun HomeScreen(
    onNavigateToCreate: () -> Unit,
    onNavigateToTitipanku: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {}
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
            HeaderSection(profileImageRes = profileImageRes)

            // Main Content
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = paddingValues.calculateBottomPadding())
            ) {
                // Title
                Text(
                    text = "Mau titip/dititipi apa?",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    modifier = Modifier.padding(top = 24.dp, bottom = 12.dp)
                )

                // Sesi Titipan Saat Ini Card
                CurrentSessionCard(participantAvatars = participantAvatars)

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
fun HeaderSection(profileImageRes: Int) {
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
                        .clickable { /* Navigate to notifications */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = "Notifikasi",
                        tint = TextPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CurrentSessionCard(participantAvatars: List<Int>) {
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
                    text = "Sesi Dititipin Saat Ini",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.Timer,
                        contentDescription = null,
                        tint = PrimaryColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "01:30:07",
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
                    painter = painterResource(id = R.drawable.ic_belanja),
                    contentDescription = "Category",
                    modifier = Modifier.size(24.dp)
                )
                
                Text(
                    text = "Alfamart Jakal",
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
                text = "Aku pakai motor jadi gabisa titip yang berat-berat ya!",
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
                // Participant Avatars - dari drawable
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

                // Detail Link
                TextButton(onClick = { /* Navigate to session detail */ }) {
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
    NavigationBar(
        containerColor = CardLight,
        modifier = Modifier.height(80.dp),
        windowInsets = WindowInsets(0, 0, 0, 0) // Remove default insets
    ) {
        // Beranda
        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Default.Home,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    "Beranda",
                    fontSize = 12.sp,
                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium
                )
            },
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryColor,
                selectedTextColor = PrimaryColor,
                indicatorColor = PrimaryColor.copy(alpha = 0.1f),
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary
            )
        )

        // Titipanku
        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Default.List,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    "Titipanku",
                    fontSize = 12.sp,
                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium
                )
            },
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryColor,
                selectedTextColor = PrimaryColor,
                indicatorColor = PrimaryColor.copy(alpha = 0.1f),
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary
            )
        )

        // Profile
        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    "Profil",
                    fontSize = 12.sp,
                    fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Medium
                )
            },
            selected = selectedTab == 2,
            onClick = { onTabSelected(2) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryColor,
                selectedTextColor = PrimaryColor,
                indicatorColor = PrimaryColor.copy(alpha = 0.1f),
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary
            )
        )
    }
}
