package com.afsar.titipin.ui.buy

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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.afsar.titipin.data.model.ChatMessage
import com.afsar.titipin.data.model.JastipOrder
import java.text.NumberFormat
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(
    onBackClick: () -> Unit,
    viewModel: TitipankuViewModel = hiltViewModel()
) {
    val session = viewModel.currentSession
    val isCreator = session?.creatorId == viewModel.currentUserId

    val allShoppingItems = viewModel.orders.filter {
        it.status == "accepted" || it.status == "bought" || it.status == "revision"
    }
    val myOrders = viewModel.orders.filter { it.requesterId == viewModel.currentUserId }

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
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF5F7FA))
                .padding(16.dp)
        ) {

            SessionStatusHeader(
                timeString = viewModel.timeString,
                isRevision = viewModel.isRevisionPhase
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isCreator) {
                // --- JASTIPER VIEW ---
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
                        Text("Tagihan Per User", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                        UserBillCard(userBills = viewModel.userBills)
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

            } else {
                // --- PENITIP VIEW ---
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

// ... (Komponen RecapCard, UserBillCard, SessionStatusHeader, ChatSection biarkan sama) ...

@Composable
fun ShoppingItemCard(
    item: JastipOrder,
    isCreator: Boolean,
    onToggleCheck: () -> Unit,
    onFlagRevision: () -> Unit
) {
    val isChecked = item.status == "bought"
    val isRevision = item.status == "revision"

    // Warna Background
    val cardColor = when {
        isChecked -> Color(0xFFF1F8E9) // Hijau muda (Dibeli)
        isRevision -> Color(0xFFFFEBEE) // Merah muda (Revisi)
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
                // Checkbox Beli
                IconButton(onClick = onToggleCheck) {
                    Icon(
                        if (isChecked) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (isChecked) Color(0xFF4CAF50) else Color.Gray
                    )
                }
            } else {
                Icon(
                    Icons.Default.ReceiptLong,
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

                // Pesan status revisi
                if (isRevision) {
                    Text("⚠️ STOK HABIS / BUTUH REVISI", color = Color.Red, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Kolom Kanan: Harga atau Tombol Revisi
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${(item.priceEstimate / 1000).toInt()}k",
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    fontSize = 12.sp
                )

                // TOMBOL REVISI (Khusus Creator & Belum dibeli)
                if (isCreator && !isChecked && !isRevision) {
                    IconButton(onClick = onFlagRevision) {
                        Icon(Icons.Default.Warning, contentDescription = "Tandai Revisi", tint = Color(0xFFFF9800))
                    }
                }
            }
        }
    }
}
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

@Composable
fun UserBillCard(userBills: Map<String, Double>) {
    val formatRp = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            userBills.forEach { (name, total) ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(name, fontSize = 14.sp)
                    Text(formatRp.format(total), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                if (userBills.keys.last() != name) Divider(color = Color.LightGray.copy(0.2f))
            }
        }
    }
}

//
//@Composable
//fun ShoppingItemCard(item: JastipOrder, isRevision: Boolean) {
//    var isChecked by remember { mutableStateOf(false) }
//
//    Card(
//        colors = CardDefaults.cardColors(containerColor = Color.White),
//        shape = RoundedCornerShape(12.dp),
//        border = if (isChecked) BorderStroke(2.dp, Color(0xFF00C853)) else null
//    ) {
//        Row(
//            modifier = Modifier
//                .padding(16.dp)
//                .fillMaxWidth(),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            IconButton(onClick = { isChecked = !isChecked }) {
//                Icon(
//                    if (isChecked) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
//                    contentDescription = null,
//                    tint = if (isChecked) Color(0xFF00C853) else Color.Gray
//                )
//            }
//
//            Column(modifier = Modifier.weight(1f)) {
//                Text(item.itemName, fontWeight = FontWeight.Bold)
//                Text("dari ${item.requesterName} (Qty: ${item.quantity})", fontSize = 12.sp, color = Color.Gray)
//                if (isRevision) {
//                    Text("Stok Habis? Revisi", color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
//                }
//            }
//        }
//    }
//}

@Composable
fun ChatSection(
    messages: List<ChatMessage>, // Menggunakan model ChatMessage
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