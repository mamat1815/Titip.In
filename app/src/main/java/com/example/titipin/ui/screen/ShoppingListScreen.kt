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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.titipin.ui.theme.*

@Composable
fun ShoppingListScreen(
    onBackClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
    onUploadClick: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToTitipanku: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    // Dummy grouped items - items with different notes are separated
    var shoppingItems by remember {
        mutableStateOf(
            listOf(
                GroupedShoppingItem(
                    itemName = "Seblak Kerupuk + Tulang",
                    totalQuantity = 2,
                    orderedBy = listOf("Budi Santoso"),
                    notes = "Level 3, pakai kerupuk banyak",
                    isChecked = false
                ),
                GroupedShoppingItem(
                    itemName = "Seblak Ceker",
                    totalQuantity = 2,
                    orderedBy = listOf("Citra Lestari"),
                    notes = "Level 1, jangan terlalu pedas",
                    isChecked = true
                ),
                GroupedShoppingItem(
                    itemName = "Seblak Seafood Komplit",
                    totalQuantity = 1,
                    orderedBy = listOf("Eko Wibowo"),
                    notes = "",
                    isChecked = false
                ),
                GroupedShoppingItem(
                    itemName = "Air Mineral",
                    totalQuantity = 1,
                    orderedBy = listOf("Budi Santoso"),
                    notes = "Aqua botol besar",
                    isChecked = false
                ),
                GroupedShoppingItem(
                    itemName = "Air Mineral",
                    totalQuantity = 1,
                    orderedBy = listOf("Eko Wibowo"),
                    notes = "Le Minerale aja",
                    isChecked = false
                )
            )
        )
    }
    
    // Countdown timer - 2 hours in seconds
    var timeRemaining by remember { mutableStateOf(2 * 60 * 60) }
    
    LaunchedEffect(Unit) {
        while (timeRemaining > 0) {
            kotlinx.coroutines.delay(1000)
            timeRemaining--
        }
    }
    
    // Format time
    val hours = timeRemaining / 3600
    val minutes = (timeRemaining % 3600) / 60
    val remainingTimeText = "$hours jam $minutes menit"
    
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
                    text = "Alfamart Jakal",
                    fontSize = 16.sp,
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                }
                
                // Header Info Card
                item {
                    ShoppingHeaderCard(
                        title = "Daftar Belanja",
                        location = "Alfamart Jakal KM 12.5",
                        itemCount = shoppingItems.size,
                        remainingTime = remainingTimeText,
                        onChatClick = onChatClick
                    )
                }
                
                // Shopping Items
                items(shoppingItems) { item ->
                    ShoppingItemCard(
                        item = item,
                        onCheckChange = { checked ->
                            shoppingItems = shoppingItems.map {
                                if (it.itemName == item.itemName) it.copy(isChecked = checked)
                                else it
                            }
                        }
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
            
            // Bottom Button
            Button(
                onClick = onUploadClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Lanjut ke Upload Struk Belanja", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ShoppingHeaderCard(
    title: String,
    location: String,
    itemCount: Int,
    remainingTime: String,
    onChatClick: () -> Unit
) {
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
                .padding(20.dp)
        ) {
            // Header with title and chat button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                
                IconButton(
                    onClick = onChatClick,
                    modifier = Modifier
                        .size(40.dp)
                        .background(PrimaryColor, CircleShape)
                ) {
                    Icon(
                        Icons.Default.Chat,
                        contentDescription = "Chat",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Location
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = location,
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Item count
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    Icons.Default.ShoppingCart,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "$itemCount Item diterima",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Timer
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = PrimaryColor,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Sisa Waktu Sesi: $remainingTime",
                    fontSize = 14.sp,
                    color = PrimaryColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun ShoppingItemCard(
    item: GroupedShoppingItem,
    onCheckChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCheckChange(!item.isChecked) }
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // QTY on left
            Text(
                text = "${item.totalQuantity}x",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary,
                modifier = Modifier.padding(top = 2.dp)
            )
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.itemName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "dari ${item.orderedBy.joinToString(", ")}",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
                
                // Notes if available
                if (item.notes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Catatan: ${item.notes}",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }
            
            // Custom circular checkbox on right
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .border(
                        width = 2.dp,
                        color = if (item.isChecked) PrimaryColor else Color(0xFFBDBDBD),
                        shape = CircleShape
                    )
                    .background(
                        if (item.isChecked) PrimaryColor else Color.Transparent
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (item.isChecked) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Checked",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// Data class
data class GroupedShoppingItem(
    val itemName: String,
    val totalQuantity: Int,
    val orderedBy: List<String>,
    val notes: String = "",
    var isChecked: Boolean = false
)
