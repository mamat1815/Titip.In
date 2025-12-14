package com.example.titipin.ui.screen

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.LocationOn
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PenitipPaymentScreen(
    onBackClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToTitipanku: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    var deliveryTime by remember { mutableStateOf("00:15:30") }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var isPaid by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = BgLight,
        topBar = {
            TopAppBar(
                title = { Text("Konfirmasi Pembayaran", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onChatClick) {
                        Image(
                            painter = painterResource(id = R.drawable.chat),
                            contentDescription = "Chat",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Column {
                // Payment Button
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { 
                            if (!isPaid) {
                                showConfirmDialog = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPaid) Color(0xFF4CAF50) else PrimaryColor
                        )
                    ) {
                        Text(
                            if (isPaid) "Pesanan Diterima" else "Konfirmasi Pembayaran",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                // Bottom Nav
                BottomNavBar(
                    selectedTab = -1,
                    onTabSelected = { index ->
                        when (index) {
                            0 -> onNavigateToHome()
                            1 -> onNavigateToTitipanku()
                            2 -> onNavigateToProfile()
                        }
                    }
                )
            }
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
            // Delivery Tracking Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.LocationOn, null, tint = Color(0xFFF57C00))
                        Text("Pembeli sedang dalam perjalanan", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.timer),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Text("Estimasi tiba: $deliveryTime", fontSize = 14.sp, color = TextSecondary)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = 0.65f,
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFF57C00)
                    )
                }
            }

            // Your Bill
            Text("Tagihan Anda", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    BillItemRow("Lemon Tea", "1 x Rp 11.000", "Rp 11.000")
                    Spacer(modifier = Modifier.height(8.dp))
                    BillItemRow("Biaya Jastip", "-", "Rp 2.000")
                    Divider(modifier = Modifier.padding(vertical = 12.dp))
                    BillItemRow("Total", "", "Rp 13.000", isBold = true)
                }
            }

            // Other Bills (Transparency)
            Text("Tagihan Lainnya", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            TransparentBillCard("Azhartama", "Rp 27.000")
            TransparentBillCard("Uqi", "Rp 52.000")
            TransparentBillCard("Marsha", "Rp 27.000")

            // Payment QR
            Text("Pembayaran", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .border(1.dp, Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(R.drawable.qr_code),
                        contentDescription = "QR Code",
                        modifier = Modifier.size(170.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Scan QRIS untuk Bayar", color = TextSecondary, fontSize = 13.sp)
                }
            }

        }
    }
    
    // Confirmation Dialog
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Konfirmasi Pembayaran") },
            text = { Text("Apakah pembeli sudah membayar dan menyerahkan barang pesanan?") },
            confirmButton = {
                Button(
                    onClick = {
                        isPaid = true
                        showConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                ) {
                    Text("Ya, Sudah")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showConfirmDialog = false },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryColor)
                ) {
                    Text("Belum")
                }
            },
            containerColor = Color.White
        )
    }
}

@Composable
fun TransparentBillCard(name: String, amount: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(name, fontSize = 14.sp)
            Text(amount, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
        }
    }
}
