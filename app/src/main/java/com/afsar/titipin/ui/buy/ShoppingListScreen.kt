package com.afsar.titipin.ui.buy

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.afsar.titipin.data.model.ChatMessage
import com.afsar.titipin.data.model.Order
import com.afsar.titipin.ui.session.detail.SessionDetailViewModel
import com.afsar.titipin.ui.payment.PaymentActivity
import com.afsar.titipin.ui.payment.PaymentStatusCard
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(
    onBackClick: () -> Unit,
    viewModel: SessionDetailViewModel = hiltViewModel()
) {
    val session = viewModel.sessionState
    val isCreator = session?.creatorId == viewModel.currentUserId

    // Filters
    val allShoppingItems = viewModel.orders.filter {
        it.status == "accepted" || it.status == "bought" || it.status == "revision"
    }
    val myOrders = viewModel.orders.filter { it.requesterId == viewModel.currentUserId }

    // Message Dialog
    if (viewModel.uiMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearMessage() },
            confirmButton = { TextButton(onClick = { viewModel.clearMessage() }) { Text("OK") } },
            text = { Text(viewModel.uiMessage!!) }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isCreator) "Daftar Belanja" else "Diskusi & Status", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            ChatSection(
                messages = viewModel.chatMessages,
                inputValue = viewModel.chatInput,
                onValueChange = { viewModel.chatInput = it },
                onSend = { viewModel.sendChat() }
            )
        }
    ) { padding ->
        if (viewModel.isLoading || session == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(Color(0xFFF5F7FA))
                    .padding(16.dp)
            ) {
                // Header Sisa Waktu
                SessionStatusHeader(
                    timeString = viewModel.timeString,
                    isRevision = viewModel.isRevisionPhase
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (isCreator) {
                    // --- TAMPILAN JASTIPER ---

                    // Rekap Total Belanjaan (Harga Barang Saja)
                    RecapCard(viewModel.totalItems, viewModel.totalPrice)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Daftar Barang (${allShoppingItems.size})", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                        items(allShoppingItems) { item ->
                            ShoppingItemCard(
                                item = item,
                                isCreator = true,
                                onToggleCheck = { viewModel.toggleItemBought(item) },
                                onFlagRevision = { viewModel.flagItemForRevision(item) }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Tagihan Per User (Estimasi)", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))

                            // Kalkulasi Tagihan Jastiper (Jika ikut beli)
                            // Note: Jastiper bayar Admin Fee juga jika pakai sistem ini, atau bisa di-exclude.
                            // Di sini kita tampilkan sesuai logic ViewModel.

                            val otherUserBills = viewModel.userBills

                            UserBillCard(
                                userBills = otherUserBills,
                                myBill = viewModel.myTotalGoodsPrice,
                                myPaymentFee = viewModel.myAdminFee,
                                myTotalWithFee = viewModel.myGrandTotal
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            // Card Pencairan Dana (Hanya muncul jika sesi ditutup)
                            if (session.status == "closed") {
                                DisbursementCard(
                                    totalAmount = viewModel.totalCollectedGoodsPrice, // Total barang lunas
                                    totalFees = 0.0, // Fee admin sudah dipisah, tidak perlu ditampilkan di sini
                                    disbursementFee = 5000.0, // Biaya Transfer Bank
                                    netAmount = viewModel.netDisbursementAmount, // Bersih diterima
                                    canDisburse = viewModel.canDisburse,
                                    disbursementStatus = viewModel.disbursementStatus,
                                    disbursementMessage = viewModel.disbursementMessage,
                                    onDisburseClick = { viewModel.requestDisbursement() },
                                    onRetryClick = { viewModel.retryDisbursement() },
                                    onDismissMessage = { viewModel.clearDisbursementMessage() }
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }

                } else {
                    // --- TAMPILAN PENITIP ---
                    val context = LocalContext.current

                    // Tampilkan status pembayaran
                    if (viewModel.orders.isNotEmpty()) {

                        PaymentStatusCard(
                            amount = viewModel.myGrandTotal, // Total yang harus dibayar
                            status = viewModel.myPaymentStatus,
                            onPayClick = {
                                if (viewModel.canIPay) {
                                    val user = viewModel.currentUser
                                    // Safety check phone number
                                    val safePhone = if (user?.phoneNumber.isNullOrEmpty()) {
                                        "08123456789" // Nomor Dummy untuk Testing
                                    } else {
                                        user.phoneNumber
                                    }
                                    val intent = Intent(context, PaymentActivity::class.java).apply {
                                        putExtra(PaymentActivity.EXTRA_SESSION_ID, session.id)
                                        putExtra(PaymentActivity.EXTRA_USER_ID, viewModel.currentUserId)
                                        // Kirim Grand Total ke Payment Activity
                                        putExtra(PaymentActivity.EXTRA_AMOUNT, viewModel.myGrandTotal.toLong())
                                        putExtra(PaymentActivity.EXTRA_USER_NAME, user?.name ?: "User")
                                        putExtra(PaymentActivity.EXTRA_USER_EMAIL, user?.email ?: "user@titipin.com")
                                        putExtra(PaymentActivity.EXTRA_USER_PHONE, safePhone)
                                    }
                                    context.startActivity(intent)
                                } else {
                                    // Pesan jika tombol diklik saat disabled (defensive)
                                    if (viewModel.myPaymentStatus == "success") {
                                        Toast.makeText(context, "Pembayaran sudah lunas!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Tunggu Jastiper menerima pesananmu dulu ya!", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    Text("Pesanan Saya", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))

                    if (myOrders.isEmpty()) {
                        Text("Kamu belum memesan apa-apa di sesi ini.", color = Color.Gray, fontSize = 12.sp)
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                            items(myOrders) { item ->
                                ShoppingItemCard(
                                    item = item,
                                    isCreator = false,
                                    onToggleCheck = {},
                                    onFlagRevision = {}
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- COMPONENTS ---

@Composable
fun ShoppingItemCard(
    item: Order,
    isCreator: Boolean,
    onToggleCheck: () -> Unit,
    onFlagRevision: () -> Unit
) {
    val isChecked = item.status == "bought"
    val isRevision = item.status == "revision"
    // Tambahan visual untuk status Accepted (Siap Bayar) vs Pending
    val isAccepted = item.status == "accepted"

    val cardColor = when {
        isChecked -> Color(0xFFE8F5E9) // Hijau Pucat (Sudah Dibeli)
        isRevision -> Color(0xFFFFEBEE) // Merah Pucat (Revisi)
        isAccepted -> Color(0xFFE3F2FD) // Biru Pucat (Siap Bayar)
        else -> Color.White
    }

    val strokeColor = if (isChecked) Color(0xFF4CAF50) else Color.Transparent

    Card(
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, strokeColor),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isCreator) {
                IconButton(onClick = onToggleCheck) {
                    Icon(
                        if (isChecked) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (isChecked) Color(0xFF4CAF50) else Color.Gray
                    )
                }
            } else {
                Icon(
                    Icons.AutoMirrored.Filled.ReceiptLong,
                    contentDescription = null,
                    tint = if (isChecked) Color(0xFF4CAF50) else if (isRevision) Color.Red else Color.Gray,
                    modifier = Modifier.padding(12.dp).size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.itemName,
                    fontWeight = FontWeight.Bold,
                    textDecoration = if (isChecked) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                    color = if (isChecked) Color.Gray else Color.Black
                )
                Text(
                    text = "${item.quantity}x | ${item.requesterName}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                if (item.notes.isNotEmpty()) {
                    Text("Catatan: ${item.notes}", fontSize = 12.sp, color = Color(0xFFEF6C00), fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                }

                if (isRevision) {
                    Text("⚠️ STOK HABIS / BUTUH REVISI", color = Color.Red, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                val formatRp = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
                Text(
                    text = formatRp.format(item.priceEstimate),
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    fontSize = 12.sp
                )

                if (isCreator && !isChecked && !isRevision) {
                    IconButton(onClick = onFlagRevision) {
                        Icon(Icons.Default.Warning, contentDescription = "Tandai Revisi", tint = Color(0xFFFF9800))
                    }
                }
            }
        }
    }
}

// ... SessionStatusHeader & RecapCard (Sama seperti sebelumnya) ...
@Composable
fun SessionStatusHeader(timeString: String, isRevision: Boolean) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Sisa Waktu Belanja", fontSize = 12.sp, color = Color.Gray)
                Text(timeString, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFFD32F2F))
            }
            if (isRevision) {
                Surface(color = Color(0xFFFFEBEE), shape = RoundedCornerShape(8.dp)) {
                    Text("⚠️ FASE REVISI", color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp))
                }
            }
        }
    }
}

@Composable
fun RecapCard(totalItems: Int, totalPrice: Double) {
    val formatRp = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8EAF6)), // Biru Muda
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.MonetizationOn, null, tint = Color(0xFF3F51B5))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("Total Estimasi ($totalItems Item)", fontSize = 12.sp, color = Color.Gray)
                Text(formatRp.format(totalPrice), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF303F9F))
            }
        }
    }
}

// --- UPDATED: UserBillCard ---
@Composable
fun UserBillCard(
    userBills: Map<String, Double>,
    myBill: Double,       // Harga Barang Murni
    myPaymentFee: Double, // Biaya Admin
    myTotalWithFee: Double // Grand Total
) {
    val formatRp = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Rincian Tagihan Saya", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(12.dp))

            // Harga Barang
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Harga Barang", fontSize = 14.sp)
                Text(formatRp.format(myBill), fontWeight = FontWeight.SemiBold)
            }

            // Biaya Layanan
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Biaya Layanan & Admin",
                    fontSize = 12.sp,
                    color = Color(0xFFE65100)
                )
                Text(
                    formatRp.format(myPaymentFee),
                    fontSize = 12.sp,
                    color = Color(0xFFE65100)
                )
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Grand Total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total Bayar", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(
                    formatRp.format(myTotalWithFee),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF1976D2)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // List Tagihan Orang Lain (Hanya Info Harga Barang)
            Text("Estimasi Member Lain:", fontSize = 12.sp, color = Color.Gray)
            userBills.forEach { (userName, amount) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(userName, fontSize = 12.sp, color = Color.Gray)
                    Text(formatRp.format(amount), fontSize = 12.sp, color = Color.Gray)
                }
            }
        }
    }
}

// ... ChatSection & DisbursementCard (Tidak banyak berubah, hanya penyesuaian parameter) ...
@Composable
fun ChatSection(
    messages: List<ChatMessage>,
    inputValue: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Chat Sesi", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.height(120.dp),
                reverseLayout = true
            ) {
                items(messages) { msg ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${msg.senderName}: ", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF370061))
                        Text(msg.message, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Input
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = inputValue,
                    onValueChange = onValueChange,
                    placeholder = { Text("Ketik pesan...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF5F5F5),
                        unfocusedContainerColor = Color(0xFFF5F5F5),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
                IconButton(onClick = onSend) {
                    Icon(Icons.Default.Send, null, tint = Color(0xFF370061))
                }
            }
        }
    }
}

@Composable
fun DisbursementCard(
    totalAmount: Double,
    totalFees: Double,
    disbursementFee: Double,
    netAmount: Double,
    canDisburse: Boolean,
    disbursementStatus: String?,
    disbursementMessage: String?,
    onDisburseClick: () -> Unit,
    onRetryClick: () -> Unit,
    onDismissMessage: () -> Unit
) {
    val formatRp = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

    // Show message dialog if exists
    if (disbursementMessage != null) {
        AlertDialog(
            onDismissRequest = onDismissMessage,
            confirmButton = {
                TextButton(onClick = onDismissMessage) {
                    Text("OK")
                }
            },
            title = {
                Text(
                    text = when (disbursementStatus) {
                        "success" -> "✅ Berhasil"
                        "failed" -> "❌ Gagal"
                        else -> "ℹ️ Informasi"
                    },
                    fontWeight = FontWeight.Bold
                )
            },
            text = { Text(disbursementMessage) }
        )
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = when (disbursementStatus) {
                "success" -> Color(0xFFE8F5E9) // Light green
                "failed" -> Color(0xFFFFEBEE) // Light red
                "loading" -> Color(0xFFFFF9C4) // Light yellow
                else -> Color(0xFFE3F2FD) // Light blue
            }
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // --- HEADER KARTU ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Cairkan Dana",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF1976D2)
                    )
                    Text(
                        "Total Barang Lunas",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        formatRp.format(totalAmount),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = Color(0xFF1565C0)
                    )

                    // Fee breakdown
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Biaya Transfer Bank: ${formatRp.format(disbursementFee)}",
                        fontSize = 11.sp,
                        color = Color(0xFFE65100)
                    )

                    Divider(modifier = Modifier.padding(vertical = 4.dp))

                    Text(
                        "Bersih Diterima:",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    Text(
                        formatRp.format(netAmount),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color(0xFF2E7D32)
                    )
                }

                Icon(
                    Icons.Default.MonetizationOn,
                    contentDescription = null,
                    tint = Color(0xFF1976D2),
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- LOGIKA STATUS & TOMBOL (Ini bagian yang Anda cari) ---
            when (disbursementStatus) {
                "success" -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF4CAF50).copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Dana sudah dicairkan!",
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                "loading" -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Sedang memproses...",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
                "failed" -> {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF44336).copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFF44336),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Gagal cairkan dana",
                                color = Color(0xFFC62828),
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onRetryClick,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                        ) {
                            Text("Coba Lagi")
                        }
                    }
                }
                else -> {
                    // Tampilan Default (Belum dicairkan)

                    // Jika belum memenuhi syarat, tampilkan peringatan
                    if (!canDisburse) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFF9800).copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ReceiptLong,
                                contentDescription = null,
                                tint = Color(0xFFFF9800),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Syarat: Sesi Tutup & Ada Saldo",
                                color = Color(0xFFE65100),
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Tombol Cairkan
                    Button(
                        onClick = onDisburseClick,
                        enabled = canDisburse && disbursementStatus != "loading",
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1976D2),
                            disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                        )
                    ) {
                        Icon(
                            Icons.Default.MonetizationOn,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (canDisburse) "Cairkan Rp ${formatRp.format(netAmount).replace("Rp", "").trim()}" else "Belum Bisa Cairkan",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
//package com.afsar.titipin.ui.buy
//
//import android.content.Intent
//import android.widget.Toast
//import androidx.compose.foundation.BorderStroke
//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.text.KeyboardOptions
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.automirrored.filled.ReceiptLong
//import androidx.compose.material.icons.filled.Add
//import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.material.icons.filled.CheckCircle
//import androidx.compose.material.icons.filled.MonetizationOn
//import androidx.compose.material.icons.filled.RadioButtonUnchecked
//import androidx.compose.material.icons.filled.ReceiptLong
//import androidx.compose.material.icons.filled.Remove
//import androidx.compose.material.icons.filled.Send
//import androidx.compose.material.icons.filled.Warning
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.hilt.navigation.compose.hiltViewModel
//import com.afsar.titipin.data.model.ChatMessage
//import com.afsar.titipin.data.model.Order
//import com.afsar.titipin.ui.session.detail.SessionDetailViewModel
//import com.afsar.titipin.ui.payment.PaymentActivity
//import com.afsar.titipin.ui.payment.PaymentStatusCard
//import java.text.NumberFormat
//import java.util.Locale
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun ShoppingListScreen(
//    onBackClick: () -> Unit,
//    viewModel: SessionDetailViewModel = hiltViewModel()
//) {
//    val session = viewModel.sessionState
//    val isCreator = session?.creatorId == viewModel.currentUserId
//
//    val allShoppingItems = viewModel.orders.filter {
//        it.status == "accepted" || it.status == "bought" || it.status == "revision" || it.status == "pending" || it.status == "rejected" || it.status == "cancelled"
//    }
//    val myOrders = viewModel.orders.filter { it.requesterId == viewModel.currentUserId }
//
//    if (viewModel.uiMessage != null) {
//        AlertDialog(
//            onDismissRequest = { viewModel.clearMessage() },
//            confirmButton = { TextButton(onClick = { viewModel.clearMessage() }) { Text("OK") } },
//            text = { Text(viewModel.uiMessage!!) }
//        )
//    }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text(if (isCreator) "Daftar Belanja" else "Diskusi & Status", fontWeight = FontWeight.Bold) },
//                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, null) } },
//                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
//            )
//        },
//        bottomBar = {
//            ChatSection(
//                messages = viewModel.chatMessages,
//                inputValue = viewModel.chatInput,
//                onValueChange = { viewModel.chatInput = it },
//                onSend = { viewModel.sendChat() }
//            )
//        }
//    ) { padding ->
//        if (viewModel.isLoading || session == null) {
//            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
//                CircularProgressIndicator()
//            }
//        } else {
//            Column(
//                modifier = Modifier
//                    .padding(padding)
//                    .fillMaxSize()
//                    .background(Color(0xFFF5F7FA))
//                    .padding(16.dp)
//            ) {
//                SessionStatusHeader(
//                    timeString = viewModel.timeString,
//                    isRevision = viewModel.isRevisionPhase
//                )
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                if (isCreator) {
//                    RecapCard(viewModel.totalItems, viewModel.totalPrice)
//                    Spacer(modifier = Modifier.height(12.dp))
//
//                    Text("Daftar Barang (${allShoppingItems.size})", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
//
//                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
//                        items(allShoppingItems) { item ->
//                            ShoppingItemCard(
//                                item = item,
//                                isCreator = true,
//                                onToggleCheck = { viewModel.toggleItemBought(item) },
//                                onFlagRevision = { viewModel.flagItemForRevision(item) }
//                            )
//                        }
//                        item {
//                            Spacer(modifier = Modifier.height(16.dp))
//                            Text("Tagihan Per User", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
////
////                            // Calculate bills excluding the creator (if they also ordered)
////                            val otherUserBills = viewModel.userBills.filter { it.key != (viewModel.currentUser?.name ?: "") }
//
//                            UserBillCard(
//                                userBills = otherUserBills,
//                                myBill = viewModel.myTotalBill,
//                                myPaymentFee = viewModel.myPaymentFee,
//                                myTotalWithFee = viewModel.myTotalWithFee
//                            )
//                            Spacer(modifier = Modifier.height(16.dp))
//
//                            // Show disbursement card only if session is closed
//                            if (session.status == "closed") {
//                                DisbursementCard(
//                                    totalAmount = viewModel.totalPrice,
//                                    totalFees = viewModel.totalPaymentFees,
//                                    disbursementFee = 2000.0,
//                                    netAmount = viewModel.netDisbursementAmount,
//                                    canDisburse = viewModel.canDisburse,
//                                    disbursementStatus = viewModel.disbursementStatus,
//                                    disbursementMessage = viewModel.disbursementMessage,
//                                    onDisburseClick = { viewModel.requestDisbursement() },
//                                    onRetryClick = { viewModel.retryDisbursement() },
//                                    onDismissMessage = { viewModel.clearDisbursementMessage() }
//                                )
//                                Spacer(modifier = Modifier.height(16.dp))
//                            }
//                        }
//                    }
//
//                } else {
//                    // --- PENITIP VIEW (Member) ---
//                    val context = LocalContext.current
//
//                    // Show Payment Card if needed
////                    if (viewModel.isPaymentRequired) {
////                        PaymentStatusCard(
////                            amount = viewModel.myTotalWithFee,
////                            status = viewModel.myPaymentStatus, // This is now derived correctly in VM
////                            onPayClick = {
////                                val user = viewModel.currentUser
////                                val intent = Intent(context, PaymentActivity::class.java).apply {
////                                    putExtra(PaymentActivity.EXTRA_SESSION_ID, session.id)
////                                    putExtra(PaymentActivity.EXTRA_USER_ID, viewModel.currentUserId)
////                                    // Cast to Long because PaymentActivity likely expects Long for amount
////                                    putExtra(PaymentActivity.EXTRA_AMOUNT, viewModel.myTotalWithFee.toLong())
////                                    putExtra(PaymentActivity.EXTRA_USER_NAME, user?.name ?: "User")
////                                    putExtra(PaymentActivity.EXTRA_USER_EMAIL, user?.email ?: "user@titipin.com")
////                                }
////                                context.startActivity(intent)
////                            },
////                            modifier = Modifier.padding(bottom = 12.dp)
////                        )
////                    }
//                    if (viewModel.isPaymentRequired) {
//
//                        // Validasi tambahan di UI
//                        val canPay = viewModel.myTotalWithFee > 0
//
//                        PaymentStatusCard(
//                            amount = viewModel.myTotalWithFee,
//                            status = viewModel.myPaymentStatus,
//                            onPayClick = {
//                                if (canPay) {
//                                    // Lanjut ke PaymentActivity
//                                    val user = viewModel.currentUser
//                                    val intent = Intent(context, PaymentActivity::class.java).apply {
//                                        putExtra(PaymentActivity.EXTRA_SESSION_ID, session.id)
//                                        putExtra(PaymentActivity.EXTRA_USER_ID, viewModel.currentUserId)
//                                        putExtra(PaymentActivity.EXTRA_AMOUNT, 20000.0)
//                                        putExtra(PaymentActivity.EXTRA_USER_NAME, user?.name ?: "User")
//                                        putExtra(PaymentActivity.EXTRA_USER_EMAIL, user?.email ?: "user@titipin.com")
//                                        // Fix phone number (di log Anda phone number isinya email)
//                                        putExtra(PaymentActivity.EXTRA_USER_PHONE, "08123456789")
//                                    }
//                                    context.startActivity(intent)
//                                } else {
//                                    // Tampilkan Peringatan jika 0
//                                    Toast.makeText(context, "Tunggu Jastiper menerima pesananmu dulu ya!", Toast.LENGTH_LONG).show()
//                                }
//                            },
//                            modifier = Modifier.padding(bottom = 12.dp)
//                            // Opsional: Anda bisa menambahkan parameter 'enabled' di PaymentStatusCard agar tombol jadi abu-abu
//                        )
//                    }
//
//                    Text("Pesanan Saya", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
//                    if (myOrders.isEmpty()) {
//                        Text("Kamu belum memesan apa-apa di sesi ini.", color = Color.Gray, fontSize = 12.sp)
//                    } else {
//                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
//                            items(myOrders) { item ->
//                                ShoppingItemCard(
//                                    item = item,
//                                    isCreator = false,
//                                    onToggleCheck = {},
//                                    onFlagRevision = {}
//                                )
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
//
//// --- COMPONENTS ---
//
//@Composable
//fun ShoppingItemCard(
//    item: Order,
//    isCreator: Boolean,
//    onToggleCheck: () -> Unit,
//    onFlagRevision: () -> Unit
//) {
//    val isChecked = item.status == "bought"
//    val isRevision = item.status == "revision"
//
//    val cardColor = when {
//        isChecked -> Color(0xFFF1F8E9) // Light Green
//        isRevision -> Color(0xFFFFEBEE) // Light Red
//        else -> Color.White
//    }
//
//    val strokeColor = if (isChecked) Color(0xFF4CAF50) else Color.Transparent
//
//    Card(
//        colors = CardDefaults.cardColors(containerColor = cardColor),
//        shape = RoundedCornerShape(12.dp),
//        border = BorderStroke(1.dp, strokeColor),
//        elevation = CardDefaults.cardElevation(1.dp),
//        modifier = Modifier.fillMaxWidth()
//    ) {
//        Row(
//            modifier = Modifier.padding(12.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            if (isCreator) {
//                IconButton(onClick = onToggleCheck) {
//                    Icon(
//                        if (isChecked) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
//                        contentDescription = null,
//                        tint = if (isChecked) Color(0xFF4CAF50) else Color.Gray
//                    )
//                }
//            } else {
//                Icon(
//                    Icons.AutoMirrored.Filled.ReceiptLong,
//                    contentDescription = null,
//                    tint = if (isChecked) Color(0xFF4CAF50) else if (isRevision) Color.Red else Color.Gray,
//                    modifier = Modifier.padding(12.dp).size(24.dp)
//                )
//            }
//
//            Column(modifier = Modifier.weight(1f)) {
//                Text(
//                    text = item.itemName,
//                    fontWeight = FontWeight.Bold,
//                    textDecoration = if (isChecked) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
//                    color = if (isChecked) Color.Gray else Color.Black
//                )
//                Text(
//                    text = "${item.quantity}x | ${item.requesterName}",
//                    fontSize = 12.sp,
//                    color = Color.Gray
//                )
//
//                if (item.notes.isNotEmpty()) {
//                    Text("Catatan: ${item.notes}", fontSize = 12.sp, color = Color(0xFFEF6C00), fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
//                }
//
//                if (isRevision) {
//                    Text("⚠️ STOK HABIS / BUTUH REVISI", color = Color.Red, fontSize = 10.sp, fontWeight = FontWeight.Bold)
//                }
//            }
//
//            Column(horizontalAlignment = Alignment.End) {
//                Text(
//                    text = "${(item.priceEstimate / 1000).toInt()}k",
//                    fontWeight = FontWeight.Bold,
//                    color = Color.Gray,
//                    fontSize = 12.sp
//                )
//
//                if (isCreator && !isChecked && !isRevision) {
//                    IconButton(onClick = onFlagRevision) {
//                        Icon(Icons.Default.Warning, contentDescription = "Tandai Revisi", tint = Color(0xFFFF9800))
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun SessionStatusHeader(timeString: String, isRevision: Boolean) {
//    Card(
//        colors = CardDefaults.cardColors(containerColor = Color.White),
//        shape = RoundedCornerShape(12.dp),
//        elevation = CardDefaults.cardElevation(2.dp),
//        modifier = Modifier.fillMaxWidth()
//    ) {
//        Row(
//            modifier = Modifier.padding(16.dp),
//            horizontalArrangement = Arrangement.SpaceBetween,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Column {
//                Text("Sisa Waktu Belanja", fontSize = 12.sp, color = Color.Gray)
//                Text(timeString, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFFD32F2F))
//            }
//            if (isRevision) {
//                Surface(color = Color(0xFFFFEBEE), shape = RoundedCornerShape(8.dp)) {
//                    Text("⚠️ FASE REVISI", color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp))
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun RecapCard(totalItems: Int, totalPrice: Double) {
//    val formatRp = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
//    Card(
//        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8EAF6)), // Light Blue
//        shape = RoundedCornerShape(12.dp),
//        modifier = Modifier.fillMaxWidth()
//    ) {
//        Row(
//            modifier = Modifier.padding(16.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Icon(Icons.Default.MonetizationOn, null, tint = Color(0xFF3F51B5))
//            Spacer(modifier = Modifier.width(12.dp))
//            Column {
//                Text("Total Estimasi ($totalItems Item)", fontSize = 12.sp, color = Color.Gray)
//                Text(formatRp.format(totalPrice), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF303F9F))
//            }
//        }
//    }
//}
//
//@Composable
//fun UserBillCard(
//    userBills: Map<String, Double>,
//    myBill: Double,
//    myPaymentFee: Double,
//    myTotalWithFee: Double
//) {
//    val formatRp = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
//
//    Card(
//        colors = CardDefaults.cardColors(containerColor = Color.Red),
//        shape = RoundedCornerShape(12.dp),
//        elevation = CardDefaults.cardElevation(2.dp),
//        modifier = Modifier.fillMaxWidth()
//    ) {
//        Column(modifier = Modifier.padding(16.dp)) {
//            Text("Tagihan Pembayaran", fontWeight = FontWeight.Bold, fontSize = 16.sp)
//            Spacer(modifier = Modifier.height(12.dp))
//
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween
//            ) {
//                Text("Total Belanja Kamu", fontSize = 14.sp)
//                Text(formatRp.format(myBill), fontWeight = FontWeight.SemiBold)
//            }
//
//            // Payment fee
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween
//            ) {
//                Text(
//                    "Biaya Admin (2.9% + Rp 2.000)",
//                    fontSize = 12.sp,
//                    color = Color(0xFFE65100)
//                )
//                Text(
//                    formatRp.format(myPaymentFee),
//                    fontSize = 12.sp,
//                    color = Color(0xFFE65100)
//                )
//            }
//
//            Divider(modifier = Modifier.padding(vertical = 8.dp))
//
//            // Total with fee
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween
//            ) {
//                Text("Total Bayar", fontWeight = FontWeight.Bold, fontSize = 16.sp)
//                Text(
//                    formatRp.format(myTotalWithFee),
//                    fontWeight = FontWeight.Bold,
//                    fontSize = 16.sp,
//                    color = Color(0xFF1976D2)
//                )
//            }
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//            // Other users' bills
//            Text("Rincian Member Lain:", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(top = 8.dp))
//            userBills.forEach { (userName, amount) ->
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(vertical = 2.dp),
//                    horizontalArrangement = Arrangement.SpaceBetween
//                ) {
//                    Text(userName, fontSize = 12.sp, color = Color.Gray)
//                    Text(formatRp.format(amount), fontSize = 12.sp, color = Color.Gray)
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun ChatSection(
//    messages: List<ChatMessage>,
//    inputValue: String,
//    onValueChange: (String) -> Unit,
//    onSend: () -> Unit
//) {
//    Card(
//        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
//        colors = CardDefaults.cardColors(containerColor = Color.White),
//        elevation = CardDefaults.cardElevation(8.dp)
//    ) {
//        Column(modifier = Modifier.padding(16.dp)) {
//            Text("Chat Sesi", fontWeight = FontWeight.Bold)
//            Spacer(modifier = Modifier.height(8.dp))
//
//            LazyColumn(
//                modifier = Modifier.height(120.dp),
//                reverseLayout = true
//            ) {
//                items(messages) { msg ->
//                    Row(verticalAlignment = Alignment.CenterVertically) {
//                        Text("${msg.senderName}: ", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF370061))
//                        Text(msg.message, fontSize = 12.sp)
//                    }
//                    Spacer(modifier = Modifier.height(4.dp))
//                }
//            }
//
//            Divider(modifier = Modifier.padding(vertical = 8.dp))
//
//            // Input
//            Row(verticalAlignment = Alignment.CenterVertically) {
//                OutlinedTextField(
//                    value = inputValue,
//                    onValueChange = onValueChange,
//                    placeholder = { Text("Ketik pesan...") },
//                    modifier = Modifier.weight(1f),
//                    shape = RoundedCornerShape(24.dp),
//                    colors = TextFieldDefaults.colors(
//                        focusedContainerColor = Color(0xFFF5F5F5),
//                        unfocusedContainerColor = Color(0xFFF5F5F5),
//                        focusedIndicatorColor = Color.Transparent,
//                        unfocusedIndicatorColor = Color.Transparent
//                    )
//                )
//                IconButton(onClick = onSend) {
//                    Icon(Icons.Default.Send, null, tint = Color(0xFF370061))
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun DisbursementCard(
//    totalAmount: Double,
//    totalFees: Double,
//    disbursementFee: Double,
//    netAmount: Double,
//    canDisburse: Boolean,
//    disbursementStatus: String?,
//    disbursementMessage: String?,
//    onDisburseClick: () -> Unit,
//    onRetryClick: () -> Unit,
//    onDismissMessage: () -> Unit
//) {
//    val formatRp = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
//
//    // Show message dialog if exists
//    if (disbursementMessage != null) {
//        AlertDialog(
//            onDismissRequest = onDismissMessage,
//            confirmButton = {
//                TextButton(onClick = onDismissMessage) {
//                    Text("OK")
//                }
//            },
//            title = {
//                Text(
//                    text = when (disbursementStatus) {
//                        "success" -> "✅ Berhasil"
//                        "failed" -> "❌ Gagal"
//                        else -> "ℹ️ Informasi"
//                    },
//                    fontWeight = FontWeight.Bold
//                )
//            },
//            text = { Text(disbursementMessage) }
//        )
//    }
//
//    Card(
//        colors = CardDefaults.cardColors(
//            containerColor = when (disbursementStatus) {
//                "success" -> Color(0xFFE8F5E9) // Light green
//                "failed" -> Color(0xFFFFEBEE) // Light red
//                "loading" -> Color(0xFFFFF9C4) // Light yellow
//                else -> Color(0xFFE3F2FD) // Light blue
//            }
//        ),
//        shape = RoundedCornerShape(12.dp),
//        elevation = CardDefaults.cardElevation(2.dp),
//        modifier = Modifier.fillMaxWidth()
//    ) {
//        Column(modifier = Modifier.padding(16.dp)) {
//            Row(
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.SpaceBetween,
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                Column(modifier = Modifier.weight(1f)) {
//                    Text(
//                        "Cairkan Dana",
//                        fontWeight = FontWeight.Bold,
//                        fontSize = 16.sp,
//                        color = Color(0xFF1976D2)
//                    )
//                    Text(
//                        "Total terkumpul",
//                        fontSize = 12.sp,
//                        color = Color.Gray
//                    )
//                    Text(
//                        formatRp.format(totalAmount),
//                        fontWeight = FontWeight.SemiBold,
//                        fontSize = 18.sp,
//                        color = Color(0xFF1565C0)
//                    )
//
//                    // Fee breakdown
//                    Spacer(modifier = Modifier.height(4.dp))
//                    Text(
//                        "Biaya transaksi: ${formatRp.format(totalFees)}",
//                        fontSize = 11.sp,
//                        color = Color(0xFFE65100)
//                    )
//                    Text(
//                        "Biaya transfer: ${formatRp.format(disbursementFee)}",
//                        fontSize = 11.sp,
//                        color = Color(0xFFE65100)
//                    )
//
//                    Divider(modifier = Modifier.padding(vertical = 4.dp))
//
//                    Text(
//                        "Yang diterima:",
//                        fontSize = 11.sp,
//                        color = Color.Gray
//                    )
//                    Text(
//                        formatRp.format(netAmount),
//                        fontWeight = FontWeight.Bold,
//                        fontSize = 20.sp,
//                        color = Color(0xFF2E7D32)
//                    )
//                }
//
//                Icon(
//                    Icons.Default.MonetizationOn,
//                    contentDescription = null,
//                    tint = Color(0xFF1976D2),
//                    modifier = Modifier.size(48.dp)
//                )
//            }
//
//            Spacer(modifier = Modifier.height(12.dp))
//
//            // Status indicator
//            when (disbursementStatus) {
//                "success" -> {
//                    Row(
//                        verticalAlignment = Alignment.CenterVertically,
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .background(Color(0xFF4CAF50).copy(alpha = 0.1f), RoundedCornerShape(8.dp))
//                            .padding(12.dp)
//                    ) {
//                        Icon(
//                            Icons.Default.CheckCircle,
//                            contentDescription = null,
//                            tint = Color(0xFF4CAF50),
//                            modifier = Modifier.size(24.dp)
//                        )
//                        Spacer(modifier = Modifier.width(8.dp))
//                        Text(
//                            "Dana sudah dicairkan!",
//                            color = Color(0xFF2E7D32),
//                            fontWeight = FontWeight.Medium
//                        )
//                    }
//                }
//                "loading" -> {
//                    Row(
//                        verticalAlignment = Alignment.CenterVertically,
//                        modifier = Modifier.fillMaxWidth()
//                    ) {
//                        CircularProgressIndicator(
//                            modifier = Modifier.size(20.dp),
//                            strokeWidth = 2.dp
//                        )
//                        Spacer(modifier = Modifier.width(12.dp))
//                        Text(
//                            "Sedang memproses...",
//                            color = Color.Gray,
//                            fontSize = 14.sp
//                        )
//                    }
//                }
//                "failed" -> {
//                    Column {
//                        Row(
//                            verticalAlignment = Alignment.CenterVertically,
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .background(Color(0xFFF44336).copy(alpha = 0.1f), RoundedCornerShape(8.dp))
//                                .padding(12.dp)
//                        ) {
//                            Icon(
//                                Icons.Default.Warning,
//                                contentDescription = null,
//                                tint = Color(0xFFF44336),
//                                modifier = Modifier.size(24.dp)
//                            )
//                            Spacer(modifier = Modifier.width(8.dp))
//                            Text(
//                                "Gagal cairkan dana",
//                                color = Color(0xFFC62828),
//                                fontWeight = FontWeight.Medium
//                            )
//                        }
//                        Spacer(modifier = Modifier.height(8.dp))
//                        Button(
//                            onClick = onRetryClick,
//                            modifier = Modifier.fillMaxWidth(),
//                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
//                        ) {
//                            Text("Coba Lagi")
//                        }
//                    }
//                }
//                else -> {
//                    // Show eligibility status
//                    if (!canDisburse) {
//                        Row(
//                            verticalAlignment = Alignment.CenterVertically,
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .background(Color(0xFFFF9800).copy(alpha = 0.1f), RoundedCornerShape(8.dp))
//                                .padding(12.dp)
//                        ) {
//                            Icon(
//                                Icons.AutoMirrored.Filled.ReceiptLong,
//                                contentDescription = null,
//                                tint = Color(0xFFFF9800),
//                                modifier = Modifier.size(20.dp)
//                            )
//                            Spacer(modifier = Modifier.width(8.dp))
//                            Text(
//                                "Tunggu semua user bayar dulu",
//                                color = Color(0xFFE65100),
//                                fontSize = 12.sp
//                            )
//                        }
//                        Spacer(modifier = Modifier.height(8.dp))
//                    }
//
//                    Button(
//                        onClick = onDisburseClick,
//                        enabled = canDisburse && disbursementStatus != "loading",
//                        modifier = Modifier.fillMaxWidth(),
//                        colors = ButtonDefaults.buttonColors(
//                            containerColor = Color(0xFF1976D2),
//                            disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
//                        )
//                    ) {
//                        Icon(
//                            Icons.Default.MonetizationOn,
//                            contentDescription = null,
//                            modifier = Modifier.size(20.dp)
//                        )
//                        Spacer(modifier = Modifier.width(8.dp))
//                        Text(
//                            if (canDisburse) "Cairkan Rp ${formatRp.format(netAmount).replace("Rp", "").trim()}" else "Belum Bisa Cairkan",
//                            fontWeight = FontWeight.Bold
//                        )
//                    }
//                }
//            }
//        }
//    }
//}