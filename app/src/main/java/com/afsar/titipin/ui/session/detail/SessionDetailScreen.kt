package com.afsar.titipin.ui.session.detail

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
import com.afsar.titipin.R
import com.afsar.titipin.data.model.Order
import com.afsar.titipin.data.model.Session
import com.afsar.titipin.ui.components.molecules.SessionInfoCard
import com.afsar.titipin.ui.components.molecules.SessionProgressBar
import com.afsar.titipin.ui.theme.OrangePrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDetailScreen(
    onBackClick: () -> Unit,
    onGoToShoppingList: (String) -> Unit, // Navigasi ke halaman belanja/pembayaran
    viewModel: SessionDetailViewModel = hiltViewModel()
) {
    val session = viewModel.sessionState
    val orders = viewModel.orders
    val currentUser = viewModel.currentUserId
    val isLoading = viewModel.isLoading

    // Cek Role & Status
    val isCreator = session?.creatorId == currentUser
    val isSessionOpen = session?.status == "open"

    // --- LOGIKA AUTO-REDIRECT ---
    // Jika status sesi berubah jadi "shopping" atau "closed", otomatis pindah ke ShoppingListScreen
    LaunchedEffect(session) {
        if (session != null) {
            val shouldRedirect = session.status == "shopping" || session.status == "closed"
            if (shouldRedirect) {
                onGoToShoppingList(session.id)
            }
        }
    }

    // State Dialog Tambah Order (Hanya Guest)
    var showAddOrderDialog by remember { mutableStateOf(false) }

    if (showAddOrderDialog) {
        AddOrderDialog(
            onDismiss = { showAddOrderDialog = false },
            onSubmit = { viewModel.createOrder(onSuccess = { showAddOrderDialog = false }) },
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

    val BgColor = Color(0xFFF9FAFB)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isCreator) "Kelola Permintaan" else "Detail Sesi",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            // FAB: Hanya untuk Guest & Sesi Masih Buka
            if (!isCreator && isSessionOpen) {
                ExtendedFloatingActionButton(
                    onClick = { showAddOrderDialog = true },
                    containerColor = OrangePrimary,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.Add, null) },
                    text = { Text("Titip Barang") }
                )
            }
        },
        bottomBar = {
            // Bottom Bar: Hanya untuk Host & Sesi Masih Buka
            if (isCreator && isSessionOpen) {
                Surface(shadowElevation = 16.dp, color = Color.White) {
                    Button(
                        onClick = {
                            // UPDATE STATUS KE 'shopping' LALU NAVIGASI
                            viewModel.startShopping {
                                session?.let { onGoToShoppingList(it.id) }
                            }
                        },
                        enabled = viewModel.isReadyToShop && !isLoading, // Aktif jika ada min 1 order diterima
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OrangePrimary,
                            disabledContainerColor = Color(0xFFFFA056),
                            disabledContentColor = Color.White
                        ),
                        shape = RoundedCornerShape(100)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Selesaikan & Mulai Belanja", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        },
        containerColor = BgColor
    ) { padding ->

        if (session == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {

                // --- HEADER SECTION (White Background) ---
                Column(
                    modifier = Modifier
                        .background(Color.White)
                        .padding(bottom = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Spacer(modifier = Modifier.height(16.dp))

                        // 1. PROGRESS BAR
                        val instruction = if (isCreator)
                            "Seleksi permintaan titipan yang masuk sebelum mulai belanja."
                        else
                            "Masukkan daftar barang yang ingin kamu titip ke temanmu."

                        SessionProgressBar(
                            currentStep = 1,
                            instructionText = instruction,
                            iconRes = R.drawable.ic_sesi
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // 2. INFO SESSION CARD
                        SessionInfoCard(
                            title = session.title,
                            description = session.description,
                            iconRes = R.drawable.ic_makanan, // TODO: Map kategori
                            durationSeconds = 1800, // TODO: Hitung sisa waktu di VM
                            showTimer = true,
                            onChatClick = null // Chat dimatikan di fase ini
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // 3. TABS (KHUSUS HOST)
                    if (isCreator) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            TitipinTabItem("Menunggu (${viewModel.pendingCount})", isSelected = viewModel.selectedTabIndex == 0) { viewModel.selectedTabIndex = 0 }
                            TitipinTabItem("Diterima (${viewModel.acceptedCount})", isSelected = viewModel.selectedTabIndex == 1) { viewModel.selectedTabIndex = 1 }
                            TitipinTabItem("Ditolak (${viewModel.rejectedCount})", isSelected = viewModel.selectedTabIndex == 2) { viewModel.selectedTabIndex = 2 }
                        }
                        HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)
                    } else {
                        // Header Guest
                        Text(
                            "Daftar Titipan Saya",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                        )
                    }
                }

                // --- LIST CONTENT ---
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Tentukan List mana yang ditampilkan
                    // Host: List difilter berdasarkan Tab
                    // Guest: List semua order miliknya
                    val listToShow = if (isCreator) {
                        viewModel.displayedOrdersForHost
                    } else {
                        orders.filter { it.requesterId == currentUser }
                    }

                    if (listToShow.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (isCreator) "Tidak ada permintaan di tab ini" else "Belum ada titipan",
                                    color = Color.Gray
                                )
                            }
                        }
                    } else {
                        items(listToShow) { order ->
                            RequestItemCard(
                                order = order,
                                isCreator = isCreator,
                                currentUserId = currentUser,
                                onAccept = { viewModel.updateOrderStatus(order.id, "accepted") },
                                onReject = { viewModel.updateOrderStatus(order.id, "rejected") }
                            )
                        }
                    }

                    // Spacer bawah agar tidak ketutup FAB/BottomBar
                    item { Spacer(modifier = Modifier.height(100.dp)) }
                }
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
                    label = { Text("Estimasi Harga Satuan (Rp)") },
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
    // ... (Kode HeaderSessionCard lama, jika ingin dipertahankan untuk debug/legacy) ...
    // Tapi di kode utama di atas kita sudah pakai SessionInfoCard yang baru.
}

@Composable
fun TimerSection(timeString: String, isRevision: Boolean, showFinishButton: Boolean, onFinishClick: () -> Unit) {
    // ... (Kode TimerSection lama) ...
    // Di kode utama di atas, fungsi timer sudah dihandle oleh SessionInfoCard.
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

    Surface(
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
        color = Color.White,
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            // 1. HEADER (Profil & Lokasi)
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(order.requesterPhotoUrl.ifEmpty { null })
                        .crossfade(true).build(),
                    contentDescription = null,
                    modifier = Modifier.size(36.dp).clip(CircleShape).background(Color.LightGray),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = if (isMyOrder) "Saya" else order.requesterName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (isMyOrder) OrangePrimary else Color.Black
                    )

                    if (order.deliveryLocation.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(text = order.deliveryLocation, fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
            Spacer(modifier = Modifier.height(8.dp))

            // 2. DAFTAR BARANG
            Text("Daftar Titipan:", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(bottom = 6.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                order.items.forEachIndexed { index, item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "${index + 1}. ${item.name}", fontSize = 13.sp, color = Color.DarkGray)
                            if (item.notes.isNotBlank()) {
                                Text(text = "Catatan: ${item.notes}", fontSize = 11.sp, color = Color.Gray, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "${item.quantity}x", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            if (item.priceEstimate > 0) {
                                Text(text = "Rp ${item.priceEstimate.toInt()}", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (order.totalEstimate > 0) {
                Text(
                    text = "Total Estimasi: Rp ${order.totalEstimate.toInt()}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = OrangePrimary,
                    modifier = Modifier.align(Alignment.End)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // 3. TOMBOL AKSI
            if (isCreator && order.status == "pending") {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onReject,
                        modifier = Modifier.weight(1f).height(36.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                        border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f)),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Tolak", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Button(
                        onClick = onAccept,
                        modifier = Modifier.weight(1f).height(36.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Terima", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                    }
                }
            } else if (order.status != "pending") {
                val (statusText, statusColor) = when(order.status) {
                    "accepted" -> "Diterima" to Color(0xFF2E7D32)
                    "rejected" -> "Ditolak" to Color.Red
                    "bought" -> "Sudah Dibeli" to Color(0xFF1565C0)
                    else -> order.status to Color.Gray
                }
                Text(
                    text = statusText,
                    color = statusColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

// --- TAB ITEM COMPONENT ---
@Composable
fun RowScope.TitipinTabItem(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .weight(1f)
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = text,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.Black else Color.Gray,
            fontSize = 13.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        if (isSelected) {
            Box(
                modifier = Modifier
                    .height(3.dp)
                    .width(30.dp)
                    .background(OrangePrimary, RoundedCornerShape(100))
            )
        }
    }
}