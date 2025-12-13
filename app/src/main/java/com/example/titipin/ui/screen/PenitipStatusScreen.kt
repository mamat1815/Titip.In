package com.example.titipin.ui.screen

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
fun PenitipStatusScreen(
    onBackClick: () -> Unit = {}
) {
    // Dummy State: Tracking vs Bill
    // Let's assume bill is ready for this demo explanation flow
    // In real app this would depend on backend status
    val isBillReady = true 

    Scaffold(
        containerColor = BgLight,
        topBar = {
            TopAppBar(
                title = { Text("Status Titipan", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            if (isBillReady) {
                Button(
                    onClick = { /* Handle Payment Confirmation */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                ) {
                    Text("Konfirmasi Pembayaran", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
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
            // Status Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Status Pesanan", fontSize = 14.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isBillReady) "Pembelian Selesai" else "Pembeli sedang berbelanja...",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isBillReady) Color(0xFF10B981) else Color(0xFFF59E0B)
                    )
                    if (!isBillReady) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            color = PrimaryColor
                        )
                    }
                }
            }
            
            if (isBillReady) {
                // Bill Summary
                Text("Rincian Tagihan", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                
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
                
                // Payment Method
                Text("Pembayaran", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                
                // QR Code Placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .border(1.dp, Color.Gray.copy(alpha=0.2f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // Using ic_belanja as placeholder for QR or replace with generate_image if allowed/neutral
                        Icon(painterResource(R.drawable.ic_belanja), null, modifier = Modifier.size(64.dp), tint = Color.Unspecified)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Scan QRIS untuk Bayar", color = TextSecondary)
                    }
                }
                
                OutlinedButton(
                    onClick = { /* Chat Pembeli */ },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Chat Pembeli (Bayar Cash)")
                }
            }
        }
    }
}

@Composable
fun BillItemRow(name: String, detail: String, price: String, isBold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(name, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal, fontSize = 14.sp)
            if (detail.isNotEmpty()) {
                Text(detail, fontSize = 12.sp, color = TextSecondary)
            }
        }
        Text(
            price, 
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal, 
            fontSize = 14.sp,
            color = if (isBold) PrimaryColor else TextPrimary
        )
    }
}
