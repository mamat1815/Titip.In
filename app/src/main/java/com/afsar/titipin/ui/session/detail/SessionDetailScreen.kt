package com.afsar.titipin.ui.session.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.afsar.titipin.data.model.Order
import com.afsar.titipin.data.model.Session

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDetailScreen(
    onBackClick: () -> Unit,
    onGoToShoppingList: (String) -> Unit,
    viewModel: SessionDetailViewModel = hiltViewModel()
) {
    val session = viewModel.sessionState
    val isCreator = session?.creatorId == viewModel.currentUserId

    var showAddOrderDialog by remember { mutableStateOf(false) }

    // Dialog Tambah Order
    if (showAddOrderDialog && !isCreator) {
        AddOrderDialog(
            onDismiss = { showAddOrderDialog = false },
            onSubmit = {
                viewModel.createOrder(onSuccess = { showAddOrderDialog = false })
            },
            itemName = viewModel.orderItemName,
            onNameChange = { viewModel.orderItemName = it },
            quantity = viewModel.orderQuantity,
            onQtyChange = { viewModel.orderQuantity = it },
            price = viewModel.orderPriceEstimate,
            onPriceChange = { viewModel.orderPriceEstimate = it },
            notes = viewModel.orderNotes,
            onNotesChange = { viewModel.orderNotes = it },
            isLoading = viewModel.isLoading
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Sesi", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        },
        floatingActionButton = {
            if (!isCreator && session?.status == "open") {
                ExtendedFloatingActionButton(
                    onClick = { showAddOrderDialog = true },
                    containerColor = Color(0xFF370061),
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.Add, "Tambah") },
                    text = { Text("Titip Barang") }
                )
            }
        },
        bottomBar = {
            // Tombol Bawah (Ringkasan / Chat)
            if (session != null) {
                val acceptedCount = viewModel.orders.count { it.status == "accepted" }
                val buttonText = if (isCreator) {
                    "Lihat Daftar Belanja ($acceptedCount Item)"
                } else {
                    "Diskusi & Lihat Belanjaan"
                }

                Button(
                    onClick = { session?.let { onGoToShoppingList(it.id) } }, // TODO: Implement navigation
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF370061))
                ) {
                    Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(buttonText)
                }
            }
        }
    ) { padding ->
        if (session == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {


                // 1. Header Card
                item { HeaderSessionCard(session) }
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = Color.Yellow)) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("--- DEBUG MODE ---", fontWeight = FontWeight.Bold)
                            Text("My ID: ${viewModel.currentUserId}")
                            Text("Creator ID: ${session.creatorId}")
                            Text("Is Creator? $isCreator") // Jika False, tombol tidak akan muncul
                            Text("Total Orders Loaded: ${viewModel.orders.size}")
                        }
                    }
                }
                // 2. Timer Countdown
                item {
                    TimerSection(
                        timeString = viewModel.timeString,
                        isRevision = viewModel.isRevisionPhase,
                        showFinishButton = isCreator && session.status == "open",
                        onFinishClick = { viewModel.finishSession() }
                    )
                }

                // 3. Filter Chips (Visual Only for now)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(selected = true, onClick = {}, label = { Text("Semua") })
                        FilterChip(selected = false, onClick = {}, label = { Text("Pending") })
                        FilterChip(selected = false, onClick = {}, label = { Text("Diterima") })
                    }
                }

                // 4. List Orders
                items(viewModel.orders) { order ->
                    RequestItemCard(
                        order = order,
                        isCreator = isCreator,
                        currentUserId = viewModel.currentUserId,
                        onAccept = { viewModel.updateOrderStatus(order.id, "accepted") },
                        onReject = { viewModel.updateOrderStatus(order.id, "rejected") }
                    )
                }

                item { Spacer(modifier = Modifier.height(100.dp)) }
            }
        }
    }
}

// --- COMPONENTS ---

@Composable
fun AddOrderDialog(
    onDismiss: () -> Unit,
    onSubmit: () -> Unit,
    itemName: String, onNameChange: (String) -> Unit,
    quantity: Int, onQtyChange: (Int) -> Unit,
    price: String, onPriceChange: (String) -> Unit,
    notes: String, onNotesChange: (String) -> Unit,
    isLoading: Boolean
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Titip Barang") },
        text = {
            Column {
                OutlinedTextField(
                    value = itemName,
                    onValueChange = onNameChange,
                    label = { Text("Nama Barang") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { if (quantity > 1) onQtyChange(quantity - 1) }) {
                        Icon(Icons.Default.Remove, null)
                    }
                    Text(quantity.toString(), fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                    IconButton(onClick = { onQtyChange(quantity + 1) }) {
                        Icon(Icons.Default.Add, null)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = price,
                    onValueChange = onPriceChange,
                    label = { Text("Estimasi Harga (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = onNotesChange,
                    label = { Text("Catatan (Warna/Rasa/dll)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = onSubmit, enabled = !isLoading) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                else Text("Kirim Titipan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
fun HeaderSessionCard(session: Session) {
    Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(
                        Brush.verticalGradient(listOf(Color(0xFF370061), Color(0xFF6200EA)))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.LocationOn, null, tint = Color.White, modifier = Modifier.size(48.dp))
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Text(session.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Text(session.locationName.ifEmpty { "Lokasi tidak diset" }, fontSize = 14.sp, color = Color.Gray)
                }
                Text("Oleh: ${session.creatorName}", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}

@Composable
fun TimerSection(timeString: String, isRevision: Boolean, showFinishButton: Boolean, onFinishClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Text("Sisa Waktu", color = Color.Gray, fontSize = 12.sp)

        Card(
            colors = CardDefaults.cardColors(containerColor = if (timeString == "Waktu Habis" || timeString == "Selesai") Color(0xFFFFEBEE) else Color(0xFFE3F2FD)),
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Text(
                text = timeString,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                color = if (timeString == "Waktu Habis" || timeString == "Selesai") Color.Red else Color(0xFF1565C0)
            )
        }

        if (showFinishButton) {
            OutlinedButton(
                onClick = onFinishClick,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Selesaikan Sesi")
            }
        }

        if (isRevision) {
            Text("⚠️ MODE REVISI AKTIF", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(top=8.dp))
        }
    }
}

@Composable
fun RequestItemCard(
    order: Order,
    isCreator: Boolean,
    currentUserId: String,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    val isMyOrder = order.requesterId == currentUserId

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Avatar Requester
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current).data(order.requesterPhotoUrl).build(),
                    contentDescription = null,
                    modifier = Modifier.size(36.dp).clip(CircleShape).background(Color.LightGray),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isMyOrder) "Saya" else order.requesterName,
                        fontWeight = FontWeight.Bold,
                        color = if (isMyOrder) Color(0xFF370061) else Color.Black
                    )
                    Text(order.itemName, fontSize = 14.sp)
                }
                Text("${order.quantity}x", fontWeight = FontWeight.Bold)
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray.copy(alpha = 0.3f))

            if (order.notes.isNotEmpty()) {
                Text("Catatan: ${order.notes}", fontSize = 13.sp, color = Color.Gray, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                Spacer(modifier = Modifier.height(4.dp))
            }
            Text("Estimasi: Rp ${order.priceEstimate.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.Medium)

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons / Status
            if (order.status == "pending") {
                if (isCreator) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = onReject,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                        ) { Text("Tolak") }

                        Button(
                            onClick = onAccept,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF370061))
                        ) { Text("Terima") }
                    }
                } else {
                    Text("Menunggu konfirmasi...", color = Color(0xFFFF9800), fontSize = 12.sp)
                }
            } else {
                val (text, color, icon) = when (order.status) {
                    "accepted" -> Triple("Diterima", Color(0xFF2E7D32), Icons.Default.CheckCircle)
                    "rejected" -> Triple("Ditolak", Color.Red, Icons.Default.Close)
                    "bought" -> Triple("Sudah Dibeli", Color(0xFF1565C0), Icons.Default.CheckCircle)
                    else -> Triple(order.status, Color.Gray, Icons.Default.CheckCircle)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text, color = color, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}