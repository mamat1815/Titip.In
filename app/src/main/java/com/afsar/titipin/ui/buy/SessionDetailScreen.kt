package com.afsar.titipin.ui.buy

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.afsar.titipin.data.model.JastipOrder
import com.afsar.titipin.data.model.JastipSession

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDetailScreen(
    session: JastipSession,
    onBackClick: () -> Unit,
    onGoToShoppingList: () -> Unit, // Navigasi ke Halaman Belanja
    viewModel: TitipankuViewModel = hiltViewModel()
) {
    // Load data detail saat masuk
    LaunchedEffect(session) { viewModel.loadSessionDetail(session) }
    var showAddOrderDialog by remember { mutableStateOf(false) }

    val isCreator = session.creatorId == viewModel.currentUserId

    if (showAddOrderDialog && !isCreator) {
        AddOrderDialog(
            onDismiss = { showAddOrderDialog = false },
            onSubmit = {
                viewModel.createOrder(onSuccess = { showAddOrderDialog = false })
            },
            itemName = viewModel.orderItemName, onNameChange = { viewModel.orderItemName = it },
            quantity = viewModel.orderQuantity, onQtyChange = { viewModel.orderQuantity = it },
            price = viewModel.orderPriceEstimate, onPriceChange = { viewModel.orderPriceEstimate = it },
            notes = viewModel.orderNotes, onNotesChange = { viewModel.orderNotes = it }
        )
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Sesi", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, null) } }
            )
        },
        floatingActionButton = {
            if (!isCreator && session.status == "open") {
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
            val acceptedCount = viewModel.orders.count { it.status == "accepted" }

            // Teks tombol beda tergantung peran, tapi fungsinya sama: Buka halaman Chat/List
            val buttonText = if (isCreator) {
                "Lihat Daftar Belanja ($acceptedCount Item)"
            } else {
                "Diskusi & Lihat Belanjaan"
            }

            Button(
                onClick = onGoToShoppingList,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF370061))
            ) {
                // Tambahkan ikon chat agar user tahu di sana ada fitur chat
                Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(buttonText)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { HeaderSessionCard(session) }

            item {
                TimerSection(
                    timeString = viewModel.timeString,
                    isRevision = viewModel.isRevisionPhase,
                    showFinishButton = isCreator, // <-- Param baru
                    onFinishClick = { /* TODO: viewModel.finishSession() */ }
                )
            }
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    FilterChip(selected = true, onClick = {}, label = { Text("Semua (${viewModel.orders.size})") })
                    FilterChip(selected = true, onClick = {}, label = { Text("Menunggu (${viewModel.orders.count { it.status == "pending" }})") })
                    FilterChip(selected = false, onClick = {}, label = { Text("Diterima") })
                    FilterChip(selected = false, onClick = {}, label = { Text("Ditolak") })
                }
            }

            items(viewModel.orders) { order ->
                RequestItemCard(
                    order = order,
                    isCreator = isCreator, // <-- KIRIM STATUS PERAN KE CARD
                    currentUserId = viewModel.currentUserId,
                    onAccept = { viewModel.updateOrderStatus(order.id, "accepted") },
                    onReject = { viewModel.updateOrderStatus(order.id, "rejected") }
                )
            }

            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }
}

@Composable
fun HeaderSessionCard(session: JastipSession) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFFD32F2F), Color(0xFFFF7043))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Text("Titipan ${session.title}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Text(session.locationName.ifEmpty { "Lokasi tidak diset" }, fontSize = 14.sp, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun TimerSection(timeString: String, isRevision: Boolean  ,  showFinishButton: Boolean, // Param baru
                 onFinishClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Text("Sesi ditutup dalam:", color = Color.Gray, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(8.dp))

        val timeParts = timeString.split(":")

        if (timeParts.size >= 2) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TimerBox(timeParts[0])
                Text(" : ", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                TimerBox(timeParts[1])
            }
        } else {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = timeString,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    color = if (timeString == "Selesai") Color(0xFFD32F2F) else Color.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        if (showFinishButton && timeString != "Selesai") {
            OutlinedButton(
                onClick = onFinishClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
            ) {
                Text("Selesaikan Sesi Lebih Awal")
            }
        }
        if (isRevision) {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))) {
                Text(
                    "⚠️ FASE REVISI 2 MENIT TERAKHIR! Segera konfirmasi stok.",
                    color = Color.Red,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}

@Composable
fun TimerBox(value: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.size(60.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
    }
}
@Composable
fun RequestItemCard(
    order: JastipOrder,
    isCreator: Boolean, // Param baru
    currentUserId: String,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    val isMyOrder = order.requesterId == currentUserId

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header User
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current).data(order.requesterPhotoUrl).build(),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.LightGray)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(order.requesterName, fontWeight = FontWeight.Bold)
                        if (isMyOrder) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(color = Color(0xFFE3F2FD), shape = RoundedCornerShape(4.dp)) {
                                Text("Saya", fontSize = 10.sp, color = Color(0xFF1565C0), modifier = Modifier.padding(4.dp))
                            }
                        }
                    }
                    Text("${order.itemName} (Qty: ${order.quantity})", fontSize = 14.sp, color = Color.Gray)
                }
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray.copy(alpha = 0.3f))

            Text("Catatan: ${order.notes}", fontSize = 14.sp)
            Text("Estimasi: Rp ${order.priceEstimate.toInt()}", fontSize = 14.sp)

            Spacer(modifier = Modifier.height(16.dp))

            // --- LOGIKA TOMBOL AKSI ---

            if (order.status == "pending") {
                if (isCreator) {
                    // JASTIPER: Lihat tombol Terima/Tolak
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = onReject, modifier = Modifier.weight(1f), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)) { Text("Tolak") }
                        Button(onClick = onAccept, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF370061))) { Text("Terima") }
                    }
                } else {
                    // PENITIP: Lihat status menunggu
                    Text("Menunggu konfirmasi...", color = Color(0xFFFF9800), fontSize = 12.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                }
            } else {
                // Status sudah Accepted/Rejected
                val statusText = if (order.status == "accepted") "Permintaan Diterima" else "Permintaan Ditolak"
                val statusColor = if (order.status == "accepted") Color(0xFF2E7D32) else Color.Red

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(if(order.status=="accepted") Icons.Default.CheckCircle else Icons.Default.Close, null, tint = statusColor, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(statusText, color = statusColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}