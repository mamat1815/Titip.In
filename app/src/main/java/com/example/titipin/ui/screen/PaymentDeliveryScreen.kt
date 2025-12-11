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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.titipin.R
import com.example.titipin.ui.theme.*

@Composable
fun PaymentDeliveryScreen(
    onBackClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
    onFinishOrder: () -> Unit = {}
) {
    var showFinishDialog by remember { mutableStateOf(false) }
    
    // Delivery timer - 30 minutes in seconds
    var deliveryTimeRemaining by remember { mutableStateOf(30 * 60) }
    
    LaunchedEffect(Unit) {
        while (deliveryTimeRemaining > 0) {
            kotlinx.coroutines.delay(1000)
            deliveryTimeRemaining--
        }
    }
    
    val deliveryMinutes = deliveryTimeRemaining / 60
    val deliverySeconds = deliveryTimeRemaining % 60
    val deliveryTimeText = String.format("%d:%02d", deliveryMinutes, deliverySeconds)
    
    // Mutable list for payment status updates
    val payments = remember {
        mutableStateListOf(
            ParticipantPayment(
                id = 1,
                name = "Andini",
                amount = 75000,
                avatarRes = R.drawable.ic_profile1,
                items = listOf(
                    PaymentOrderItem("Indomie Goreng", 2, "Rasa rendang"),
                    PaymentOrderItem("Teh Pucuk", 1, "")
                ),
                initialStatus = "Menunggu Pembayaran"
            ),
            ParticipantPayment(
                id = 2,
                name = "Budi",
                amount = 60000,
                avatarRes = R.drawable.ic_profile2,
                items = listOf(
                    PaymentOrderItem("Susu Ultra Milk Cokelat", 1, ""),
                    PaymentOrderItem("Air Mineral", 2, "Aqua botol besar")
                ),
                initialStatus = "Sudah Bayar"
            )
        )
    }
    
    val totalAmount = payments.sumOf { it.amount }
    
    if (showFinishDialog) {
        AlertDialog(
            onDismissRequest = { showFinishDialog = false },
            title = { Text(text = "Selesaikan Pesanan") },
            text = { Text(text = "Apakah Anda yakin seluruh proses pesanan dan pengantaran sudah selesai?") },
            confirmButton = {
                Button(
                    onClick = {
                        showFinishDialog = false
                        onFinishOrder()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                ) {
                    Text("Ya, Selesai")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showFinishDialog = false },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryColor)
                ) {
                    Text("Tidak")
                }
            },
            containerColor = Color.White,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary
        )
    }
    
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
                    text = "Konfirmasi Pembayaran",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
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
            
            // Maps Tracking Card - Expanded
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box {
                        // Dummy Maps Image - Taller
                        Image(
                            painter = painterResource(id = R.drawable.alfamart),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp), // Expanded height
                            contentScale = ContentScale.Crop
                        )
                        
                        // Overlay
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                                .background(
                                    androidx.compose.ui.graphics.Brush.verticalGradient(
                                        listOf(
                                            Color.Transparent,
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.7f)
                                        )
                                    )
                                )
                        )
                        
                        // Content
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "Waktu Mengantar",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Text(
                                text = deliveryTimeText,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
            
            // Payment Summary Card with Chat
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        // Header with Chat Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Ringkasan Pembayaran",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            
                            // Small Chat Button
                            IconButton(
                                onClick = onChatClick,
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(PrimaryColor, CircleShape)
                            ) {
                                Icon(
                                    Icons.Default.Chat,
                                    contentDescription = "Chat",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Total Bayar",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "Rp ${formatCurrency(totalAmount)}",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryColor
                        )
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        Divider(color = Color(0xFFE0E0E0))
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        payments.forEachIndexed { index, payment ->
                            PaymentPersonCard(
                                payment = payment,
                                onPayCash = {
                                    // Update payment status to "Sudah Bayar"
                                    payments[index] = payment.copy(status = "Sudah Bayar")
                                }
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }
            
            // QR Code Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "QR Code Pembayaran",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // QR Code Placeholder
                        Box(
                            modifier = Modifier
                                .size(200.dp)
                                .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
                                .border(2.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.QrCode2,
                                contentDescription = "QR Code",
                                modifier = Modifier.size(150.dp),
                                tint = TextSecondary
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = "Scan QR code ini dengan aplikasi\ne-wallet kamu untuk pembayaran",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
            
            // Finish Order Button
            item {
                Button(
                    onClick = { showFinishDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Selesaikan Pesanan",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun PaymentPersonCard(
    payment: ParticipantPayment,
    onPayCash: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Person header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = payment.avatarRes),
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                
                Column {
                    Text(
                        text = payment.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    
                    // Status Badge
                    val isPaid = payment.status == "Sudah Bayar"
                    Text(
                        text = payment.status,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isPaid) Color(0xFF4CAF50) else Color(0xFFFF9800)
                    )
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Rp ${formatCurrency(payment.amount)}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryColor
                )
                
                if (payment.status == "Menunggu Pembayaran") {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onPayCash,
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        border = BorderStroke(1.dp, PrimaryColor),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "Bayar Cash",
                            fontSize = 11.sp,
                            color = PrimaryColor
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Items list
        payment.items.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 52.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "• ${item.name}",
                        fontSize = 13.sp,
                        color = TextPrimary
                    )
                    if (item.notes.isNotEmpty()) {
                        Text(
                            text = "  ${item.notes}",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
                
                Text(
                    text = "${item.quantity}x",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}

fun formatCurrency(amount: Int): String {
    return String.format("%,d", amount).replace(',', '.')
}

// Data classes
data class ParticipantPayment(
    val id: Int,
    val name: String,
    val amount: Int,
    val avatarRes: Int,
    val items: List<PaymentOrderItem>,
    val initialStatus: String,
    val status: String = initialStatus
)

data class PaymentOrderItem(
    val name: String,
    val quantity: Int,
    val notes: String
)
