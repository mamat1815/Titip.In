package com.example.titipin.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.titipin.R
import com.example.titipin.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onBackClick: () -> Unit = {}
) {
    // Tabs
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Titipan", "Dititipi")
    
    // Categories for filter
    val categories = listOf("Semua", "Makanan", "Obat-obatan", "Belanjaan")
    var selectedCategory by remember { mutableStateOf("Semua") }

    // Dummy Data - Titipan (Requests)
    val titipanFebruaryItems = listOf(
        HistoryItem("28 Coffee", "28 Feb 2024", "Rp 25.000", "Makanan", R.drawable.ic_makanan),
        HistoryItem("Apotek K24", "25 Feb 2024", "Rp 160.000", "Obat-obatan", R.drawable.ic_obat)
    )
    val titipanJanuaryItems = listOf(
        HistoryItem("Indomaret Point", "30 Jan 2024", "Rp 45.000", "Belanjaan", R.drawable.ic_belanja),
        HistoryItem("Sate Pak Pong", "20 Jan 2024", "Rp 120.000", "Makanan", R.drawable.ic_makanan)
    )

    // Dummy Data - Dititipi (Orders)
    val dititipiFebruaryItems = listOf(
        HistoryItem("Superindo", "20 Feb 2024", "Rp 350.000", "Belanjaan", R.drawable.ic_belanja),
        HistoryItem("Mixue", "15 Feb 2024", "Rp 18.000", "Makanan", R.drawable.ic_makanan)
    )
    val dititipiJanuaryItems = listOf(
        HistoryItem("Kimia Farma", "25 Jan 2024", "Rp 75.000", "Obat-obatan", R.drawable.ic_obat),
        HistoryItem("Alfamart", "15 Jan 2024", "Rp 30.000", "Belanjaan", R.drawable.ic_belanja),
        HistoryItem("Bakso Idolaku", "10 Jan 2024", "Rp 20.000", "Makanan", R.drawable.ic_makanan)
    )

    // Select items based on Tab
    val currentFebruaryItems = if (selectedTab == 0) titipanFebruaryItems else dititipiFebruaryItems
    val currentJanuaryItems = if (selectedTab == 0) titipanJanuaryItems else dititipiJanuaryItems

    // Filter Logic
    val filteredFebruaryItems = if (selectedCategory == "Semua") currentFebruaryItems else currentFebruaryItems.filter { it.category == selectedCategory }
    val filteredJanuaryItems = if (selectedCategory == "Semua") currentJanuaryItems else currentJanuaryItems.filter { it.category == selectedCategory }

    Scaffold(
        containerColor = Color(0xFFF5F5F5),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(56.dp)
                        .padding(horizontal = 16.dp)
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Back", 
                            tint = TextPrimary
                        )
                    }
                    Text(
                        text = "Riwayat Pesanan",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                // Tabs Row
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.White,
                    contentColor = PrimaryColor,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = PrimaryColor,
                            height = 3.dp
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
                                    fontWeight = if(selectedTab == index) FontWeight.Bold else FontWeight.Normal
                                ) 
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    // .background(Color.White) // Removed background as requested
                    .padding(vertical = 12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    val iconRes = when (category) {
                        "Makanan" -> R.drawable.ic_makanan
                        "Obat-obatan" -> R.drawable.ic_obat
                        "Belanjaan" -> R.drawable.ic_belanja
                        else -> null
                    }
                    
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category) },
                        leadingIcon = if (iconRes != null) {
                            {
                                Icon(
                                    painter = painterResource(id = iconRes),
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = Color.Unspecified
                                )
                            }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryColor,
                            selectedLabelColor = Color.White,
                            containerColor = Color.White,
                            labelColor = TextPrimary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedCategory == category,
                            borderColor = if (selectedCategory == category) PrimaryColor else Color(0xFFE0E0E0),
                            selectedBorderColor = PrimaryColor,
                            borderWidth = 1.dp
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // February Section
                if (filteredFebruaryItems.isNotEmpty()) {
                    item {
                        Text(
                            text = "Februari 2024",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    items(filteredFebruaryItems) { item ->
                        HistoryCard(item)
                    }
                }

                // Spacing between months
                if (filteredFebruaryItems.isNotEmpty() && filteredJanuaryItems.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // January Section
                if (filteredJanuaryItems.isNotEmpty()) {
                    item {
                        Text(
                            text = "Januari 2024",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    items(filteredJanuaryItems) { item ->
                        HistoryCard(item)
                    }
                }
                
                // Empty State
                if (filteredFebruaryItems.isEmpty() && filteredJanuaryItems.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 64.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Tidak ada riwayat ${tabs[selectedTab].lowercase()}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                            Text(
                                "untuk kategori ini",
                                fontSize = 14.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryCard(item: HistoryItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Background
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = item.iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    contentScale = ContentScale.Fit
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.date,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = item.amount,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .background(
                            color = Color(0xFFC8E6C9), // Always Green Background (Light)
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Selesai",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF2E7D32) // Always Green Text (Dark)
                    )
                }
            }
        }
    }
}

data class HistoryItem(
    val title: String,
    val date: String,
    val amount: String,
    val category: String,
    val iconRes: Int
)
