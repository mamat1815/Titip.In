package com.afsar.titipin.ui.buy

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MonetizationOn
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.afsar.titipin.R
import com.afsar.titipin.data.model.ChatMessage
import com.afsar.titipin.data.model.Order
import com.afsar.titipin.data.model.OrderItem
import com.afsar.titipin.ui.components.molecules.SessionInfoCard
import com.afsar.titipin.ui.components.molecules.SessionProgressBar
import com.afsar.titipin.ui.payment.PaymentActivity
import com.afsar.titipin.ui.payment.PaymentStatusCard
import com.afsar.titipin.ui.session.detail.SessionDetailViewModel
import com.afsar.titipin.ui.theme.OrangePrimary
import com.afsar.titipin.ui.theme.TextPrimary
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

    // LOGIKA KUNCI: Host hanya bisa edit jika status "shopping"
    // Jika "closed" (apalagi sudah cair), semua aksi edit dimatikan.
    val isEditable = isCreator && session?.status == "shopping"

    // Filter Items
    val shoppingOrders = viewModel.orders.filter {
        it.status == "accepted" || it.status == "bought"
    }
    val myOrders = viewModel.orders.filter { it.requesterId == viewModel.currentUserId }

    // Chat Bottom Sheet State
    var showChatSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    // State Dialog Edit Harga
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedOrderToEdit by remember { mutableStateOf<Order?>(null) }
    var selectedItemIndex by remember { mutableStateOf(-1) }
    var selectedItemPrice by remember { mutableStateOf(0.0) }

    val BgColor = Color(0xFFF9FAFB)
    val context = LocalContext.current

    // Message Alert
    if (viewModel.uiMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearMessage() },
            confirmButton = { TextButton(onClick = { viewModel.clearMessage() }) { Text("OK") } },
            text = { Text(viewModel.uiMessage!!) }
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(BgColor)) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ==========================================
            // --- HEADER ---
            // ==========================================
            Column(
                modifier = Modifier
                    .background(Color.White)
                    .statusBarsPadding()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 12.dp)
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                    Text(
                        text = if (isCreator) "Daftar Belanja" else "Status Belanja",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    SessionProgressBar(
                        currentStep = 2,
                        instructionText = if (isCreator) "Ceklis barang & sesuaikan harga jika perlu!" else "Pantau status belanjaanmu.",
                        iconRes = R.drawable.ic_sesi
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (session != null) {
                        SessionInfoCard(
                            title = session.title,
                            description = session.description,
                            iconRes = R.drawable.ic_makanan,
                            durationSeconds = 0,
                            onChatClick = { showChatSheet = true }
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)
            }

            // ==========================================
            // --- LIST CONTENT ---
            // ==========================================
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .background(BgColor),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 120.dp)
            ) {
                if (isCreator) {
                    // === TAMPILAN JASTIPER (HOST) ===
                    items(shoppingOrders) { order ->
                        order.items.forEachIndexed { index, item ->
                            ShoppingChecklistCard(
                                item = item,
                                requesterName = order.requesterName,
                                isChecked = item.status == "bought",
                                isRevision = item.status == "revision",
                                isCreator = true,

                                // --- LOGIKA KLIK & EDIT (DIPERBAIKI) ---
                                onToggle = {
                                    if (isEditable) viewModel.toggleItemStatus(order, index)
                                    else Toast.makeText(context, "Sesi ditutup, tidak bisa ubah status.", Toast.LENGTH_SHORT).show()
                                },
                                onLongClick = {
                                    if (isEditable) viewModel.flagItemRevision(order, index)
                                },
                                onEditPriceClick = {
                                    if (isEditable) {
                                        selectedOrderToEdit = order
                                        selectedItemIndex = index
                                        selectedItemPrice = item.priceEstimate
                                        showEditDialog = true
                                    } else {
                                        Toast.makeText(context, "Sesi ditutup, tidak bisa ubah harga.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                // ---------------------------------------
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    // Footer Tagihan & Disbursement
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        if (viewModel.myTotalGoodsPrice > 0) {
                            Text("Tagihan Saya", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                            UserBillCard(
                                myBill = viewModel.myTotalGoodsPrice,
                                myJastipFee = viewModel.myTotalJastipFee,
                                myPaymentFee = viewModel.myAdminFee,
                                myTotalWithFee = viewModel.myGrandTotal
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // Kartu Pencairan Dana (Hanya Muncul kalau Sesi Closed)
                        if (session?.status == "closed") {
                            DisbursementCard(
                                totalAmount = viewModel.totalCollectedGoodsPrice,
                                disbursementFee = 5000.0,
                                netAmount = viewModel.netDisbursementAmount,
                                canDisburse = viewModel.canDisburse,
                                disbursementStatus = viewModel.disbursementStatus,
                                disbursementMessage = viewModel.disbursementMessage,
                                onDisburseClick = { viewModel.requestDisbursement() },
                                onRetryClick = { viewModel.retryDisbursement() },
                                onDismissMessage = { viewModel.clearDisbursementMessage() }
                            )
                        }
                    }

                } else {
                    // === TAMPILAN GUEST ===
                    item {
                        if (viewModel.myTotalGoodsPrice > 0) {
                            PaymentStatusCard(
                                amount = viewModel.myGrandTotal,
                                status = viewModel.myPaymentStatus,
                                onPayClick = {
                                    if (viewModel.canIPay) {
                                        val user = viewModel.currentUser
                                        val safePhone = user?.phoneNumber.takeIf { !it.isNullOrEmpty() } ?: "08123456789"
                                        val subTotal = viewModel.mySubTotal.toLong()
                                        val intent = Intent(context, PaymentActivity::class.java).apply {
                                            putExtra(PaymentActivity.EXTRA_SESSION_ID, session?.id)
                                            putExtra(PaymentActivity.EXTRA_USER_ID, viewModel.currentUserId)
                                            putExtra(PaymentActivity.EXTRA_AMOUNT, subTotal)
                                            putExtra(PaymentActivity.EXTRA_USER_NAME, user?.name ?: "User")
                                            putExtra(PaymentActivity.EXTRA_USER_EMAIL, user?.email ?: "user@titipin.com")
                                            putExtra(PaymentActivity.EXTRA_USER_PHONE, safePhone)
                                        }
                                        context.startActivity(intent)
                                    } else {
                                        val msg = if (viewModel.myPaymentStatus == "success") "Pembayaran lunas!" else "Menunggu konfirmasi..."
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            UserBillCard(
                                myBill = viewModel.myTotalGoodsPrice,
                                myJastipFee = viewModel.myTotalJastipFee,
                                myPaymentFee = viewModel.myAdminFee,
                                myTotalWithFee = viewModel.myGrandTotal
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        Text("Pesanan Saya", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (myOrders.isEmpty()) {
                        item { Text("Kamu belum memesan apa-apa.", color = Color.Gray, fontSize = 12.sp) }
                    } else {
                        items(myOrders) { order ->
                            order.items.forEach { item ->
                                ShoppingChecklistCard(
                                    item = item,
                                    requesterName = "Saya",
                                    isChecked = item.status == "bought",
                                    isRevision = item.status == "revision",
                                    onToggle = {},
                                    isCreator = false
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }

        // === BUTTON SELESAI BELANJA (Hanya jika status 'shopping') ===
        if (isCreator && session?.status == "shopping") {
            Button(
                onClick = { viewModel.finishSession() },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 40.dp, start = 16.dp, end = 16.dp)
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                shape = RoundedCornerShape(100)
            ) {
                Text("Selesai Belanja & Tutup Sesi", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        // === CHAT SHEET ===
        if (showChatSheet) {
            ModalBottomSheet(
                onDismissRequest = { showChatSheet = false },
                sheetState = sheetState,
                containerColor = Color.White
            ) {
                ChatSection(
                    messages = viewModel.chatMessages,
                    inputValue = viewModel.chatInput,
                    onValueChange = { viewModel.chatInput = it },
                    onSend = { viewModel.sendChat() }
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // === DIALOG EDIT HARGA ===
        if (showEditDialog && selectedOrderToEdit != null) {
            EditPriceDialog(
                initialPrice = selectedItemPrice,
                onDismiss = { showEditDialog = false },
                onConfirm = { newPrice ->
                    viewModel.updateItemPrice(
                        orderId = selectedOrderToEdit!!.id,
                        itemIndex = selectedItemIndex,
                        newPrice = newPrice
                    )
                    showEditDialog = false
                }
            )
        }
    }
}

// ==========================================
// --- KOMPONEN PENDUKUNG ---
// ==========================================

@Composable
fun EditPriceDialog(
    initialPrice: Double,
    onConfirm: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var priceText by remember { mutableStateOf(initialPrice.toInt().toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ubah Harga Barang") },
        text = {
            Column {
                Text("Masukkan harga real/asli (sesuai struk):", fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { input -> if (input.all { it.isDigit() }) priceText = input },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = { Text("Harga Baru (Rp)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val newPrice = priceText.toDoubleOrNull() ?: initialPrice
                onConfirm(newPrice)
            }) { Text("Simpan") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
fun ShoppingChecklistCard(
    item: OrderItem,
    requesterName: String,
    isChecked: Boolean,
    isRevision: Boolean,
    onToggle: () -> Unit,
    onLongClick: () -> Unit = {},
    isCreator: Boolean = true,
    onEditPriceClick: () -> Unit = {}
) {
    val formatRp = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

    val borderColor = when {
        isRevision -> Color.Red
        isChecked -> OrangePrimary
        else -> Color(0xFFE0E0E0)
    }

    val backgroundColor = when {
        isRevision -> Color(0xFFFFEBEE)
        isChecked -> Color(0xFFFFF3E0)
        else -> Color.White
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .clickable(enabled = isCreator) { onToggle() } // Tetap clickable, logic isEditable di handle parent
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // KIRI: Detail
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${item.quantity}x ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = OrangePrimary
                    )
                    Text(
                        text = item.name,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = if (isChecked) Color.Gray else TextPrimary,
                        textDecoration = if (isChecked) TextDecoration.LineThrough else null
                    )
                }

                if (isCreator) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = formatRp.format(item.priceEstimate * item.quantity),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                        IconButton(
                            onClick = onEditPriceClick,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Ubah Harga",
                                tint = Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text("Punya: $requesterName", fontSize = 12.sp, color = Color.Gray)

                if (item.notes.isNotEmpty()) {
                    Text("Note: ${item.notes}", fontSize = 11.sp, color = Color(0xFFEF6C00), fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                }
                if (isRevision) {
                    Text("⚠️ STOK HABIS / PERLU REVISI", fontSize = 10.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                }
            }

            // KANAN: Checkbox
            if (isCreator) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            color = if (isChecked) OrangePrimary else Color(0xFFEEEEEE),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isChecked) {
                        Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    } else if (isRevision) {
                        Icon(Icons.Default.Warning, null, tint = Color.Red, modifier = Modifier.size(16.dp))
                    }
                }
            } else {
                Text(
                    text = formatRp.format(item.priceEstimate * item.quantity),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun UserBillCard(
    myBill: Double,
    myJastipFee: Double,
    myPaymentFee: Double,
    myTotalWithFee: Double
) {
    val formatRp = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Rincian Biaya", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))

            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text("Harga Barang", fontSize = 13.sp)
                Text(formatRp.format(myBill), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
            if (myJastipFee > 0) {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Text("Jasa Titip (Tip)", fontSize = 13.sp)
                    Text(formatRp.format(myJastipFee), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text("Biaya Layanan (2% + 2.500)", fontSize = 12.sp, color = Color(0xFFE65100))
                Text(formatRp.format(myPaymentFee), fontSize = 12.sp, color = Color(0xFFE65100))
            }
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text("Total Bayar", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(formatRp.format(myTotalWithFee), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1976D2))
            }
        }
    }
}

@Composable
fun DisbursementCard(
    totalAmount: Double,
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
    val isDisbursed = disbursementStatus == "success" || disbursementStatus == "completed"

    if (disbursementMessage != null) {
        AlertDialog(
            onDismissRequest = onDismissMessage,
            confirmButton = { TextButton(onClick = onDismissMessage) { Text("OK") } },
            title = {
                Text(
                    text = when (disbursementStatus) {
                        "success", "completed" -> "✅ Berhasil"
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
                "success", "completed" -> Color(0xFFE8F5E9)
                "failed" -> Color(0xFFFFEBEE)
                "loading" -> Color(0xFFFFF9C4)
                else -> Color(0xFFE3F2FD)
            }
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Cairkan Dana", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1976D2))
                    Text("Total Saldo (Barang + Fee)", fontSize = 12.sp, color = Color.Gray)
                    Text(formatRp.format(totalAmount), fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = Color(0xFF1565C0))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Biaya Transfer Bank: ${formatRp.format(disbursementFee)}", fontSize = 11.sp, color = Color(0xFFE65100))
                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                    Text("Bersih Diterima:", fontSize = 11.sp, color = Color.Gray)
                    Text(formatRp.format(netAmount), fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF2E7D32))
                }
                if (isDisbursed) {
                    Icon(Icons.Default.Check, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(48.dp))
                } else {
                    Icon(Icons.Default.MonetizationOn, null, tint = Color(0xFF1976D2), modifier = Modifier.size(48.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (disbursementStatus) {
                "loading" -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Sedang memproses...", color = Color.Gray)
                    }
                }
                else -> {
                    Button(
                        onClick = onDisburseClick,
                        enabled = canDisburse && disbursementStatus != "loading" && !isDisbursed,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDisbursed) Color.Gray else Color(0xFF1976D2),
                            disabledContainerColor = if (isDisbursed) Color(0xFFA5D6A7) else Color.LightGray
                        )
                    ) {
                        Text(
                            text = when {
                                isDisbursed -> "✅ Dana Sudah Dicairkan"
                                canDisburse -> "Cairkan Dana"
                                else -> "Belum Bisa Cairkan"
                            },
                            fontWeight = FontWeight.Bold,
                            color = if (isDisbursed) Color(0xFF1B5E20) else Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatSection(
    messages: List<ChatMessage>,
    inputValue: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Chat Sesi", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(modifier = Modifier.height(200.dp), reverseLayout = true) {
            items(messages) { msg ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${msg.senderName}: ", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF370061))
                    Text(msg.message, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
        Divider(modifier = Modifier.padding(vertical = 8.dp))
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