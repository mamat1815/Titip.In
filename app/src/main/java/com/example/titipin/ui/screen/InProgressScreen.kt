package com.example.titipin.ui.screen

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.titipin.R
import com.example.titipin.ui.theme.*
import com.example.titipin.ui.viewmodel.InProgressViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InProgressScreen(
    onBackClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
    onAutoNavigateToAssignment: () -> Unit = {},
    viewModel: InProgressViewModel = viewModel()
) {
    val timeRemaining by viewModel.timeRemaining.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()

    Scaffold(
        containerColor = BgLight,
        topBar = {
            TopAppBar(
                title = { Text("Sedang Diproses", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onChatClick) {
                        Icon(Icons.Default.Chat, "Chat", tint = PrimaryColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Timer Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.Timer, null, tint = Color(0xFFF57C00))
                    Column {
                        Text("Waktu Belanja", fontSize = 12.sp, color = TextSecondary)
                        Text(
                            timeRemaining,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF57C00)
                        )
                    }
                }
            }

            // Status Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isScanning) Color(0xFFE8F5E9) else Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.ShoppingCart,
                        null,
                        tint = if (isScanning) Color(0xFF2E7D32) else PrimaryColor
                    )
                    Column {
                        Text(
                            if (isScanning) "Pembeli sudah selesai belanja!" else "Pembeli sedang berbelanja...",
                            fontWeight = FontWeight.Bold,
                            color = if (isScanning) Color(0xFF2E7D32) else TextPrimary
                        )
                        if (isScanning) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Menunggu pembagian item...",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            // Your Order
            Text("Pesanan Anda", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            OrderItemCard("Lemon Tea", 1, "Yang dingin ya", R.drawable.ic_makanan)

            // Other Orders
            Text("Pesanan Lainnya", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            OrderItemCard("Cheezy Freezy M", 1, "", R.drawable.ic_makanan, "Azhartama")
            OrderItemCard("Roti Tawar", 2, "Jangan yang expired", R.drawable.ic_belanja, "Uqi")
            OrderItemCard("Red Bull M", 1, "Sugar free", R.drawable.ic_makanan, "Marsha")

            // Dummy Auto-navigate button for testing
            if (isScanning) {
                Button(
                    onClick = onAutoNavigateToAssignment,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                ) {
                    Text("Lanjut ke Pembagian Item (Dummy)")
                }
            }
        }
    }
}

@Composable
fun OrderItemCard(
    item: String,
    qty: Int,
    notes: String,
    iconRes: Int,
    orderedBy: String? = null
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                orderedBy?.let {
                    Text(it, fontSize = 12.sp, color = PrimaryColor, fontWeight = FontWeight.Bold)
                }
                Text(item, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                Text("Qty: $qty", fontSize = 12.sp, color = TextSecondary)
                if (notes.isNotEmpty()) {
                    Text(
                        "\"$notes\"",
                        fontSize = 12.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}
