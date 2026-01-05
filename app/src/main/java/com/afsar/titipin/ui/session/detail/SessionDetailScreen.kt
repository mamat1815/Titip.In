package com.afsar.titipin.ui.session.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.afsar.titipin.ui.components.molecules.SessionInfoCard
import com.afsar.titipin.ui.components.molecules.SessionProgressBar
import com.afsar.titipin.ui.theme.OrangePrimary


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDetailScreen(
    onBackClick: () -> Unit,
    onGoToShoppingList: (String) -> Unit,
    onGoToPayment: (String) -> Unit,
    onGoToHome: () -> Unit,
    viewModel: SessionDetailViewModel = hiltViewModel()
) {
    val session = viewModel.sessionState
    val orders = viewModel.orders
    val currentUser = viewModel.currentUserId
    val isLoading = viewModel.isLoading

    val isCreator = session?.creatorId == currentUser
    var icSesi = R.drawable.ic_sesi
    if (isCreator) {
        icSesi = R.drawable.ic_sesi
    } else {

        icSesi = R.drawable.ic_titip
    }

    val status = session?.status ?: "open"
    val isSessionOpen = status == "open"

    val currentStep = when (status) {
        "open" -> 1
        "shopping" -> 2
        "settling" -> 3
        else -> 1
    }


    val remainingSeconds = remember(session) {
        if (session != null) viewModel.getRemainingTimeInSeconds(session) else 0L
    }

    var showAddOrderDialog by remember { mutableStateOf(false) }

    if (showAddOrderDialog) {
        AddOrderDialog(
            onDismiss = { showAddOrderDialog = false },
            onSubmit = {
                viewModel.createOrder(onSuccess = {
                    showAddOrderDialog = false
                    viewModel.resetOrderForm()
                })
            },
            itemName = viewModel.orderItemName,
            onNameChange = { viewModel.orderItemName = it },
            quantity = viewModel.orderQuantity,
            onQtyChange = { viewModel.orderQuantity = it },
            price = viewModel.orderPriceEstimate,
            onPriceChange = { viewModel.orderPriceEstimate = it },
            notes = viewModel.orderNotes,
            onNotesChange = { viewModel.orderNotes = it },
            deliveryLocation = viewModel.orderDeliveryLocation,
            onLocationChange = { viewModel.orderDeliveryLocation = it },
            isLoading = viewModel.isLoading
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isCreator) "Kelola Sesi" else "Detail Sesi",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        if (session != null) {
                            Text(
                                text = "Tujuan: ${session.locationName}",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
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
            Surface(shadowElevation = 16.dp, color = Color.White) {
                Column(modifier = Modifier.padding(16.dp)) {

                    if (isCreator && isSessionOpen) {
                        Button(
                            onClick = {
                                viewModel.startShopping {
                                    onGoToShoppingList(session.id)
                                }
                            },
                            enabled = viewModel.isReadyToShop && !isLoading,
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                            shape = RoundedCornerShape(100)
                        ) {
                            if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            else Text("Tutup Order & Mulai Belanja", fontWeight = FontWeight.Bold)
                        }
                    }

                    else if (isCreator && (status == "shopping" || status == "settling")) {
                        Button(
                            onClick = { onGoToShoppingList(session.id) },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                            shape = RoundedCornerShape(100)
                        ) {
                            Text("Buka Daftar Belanja", fontWeight = FontWeight.Bold)
                        }
                    }

                    else if (!isCreator && (status == "settling" || status == "closed")) {
                        Button(
                            onClick = { onGoToPayment(session!!.id) },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            shape = RoundedCornerShape(100)
                        ) {
                            Text("Rincian Pembayaran", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFFF9FAFB)
    ) { padding ->

        if (session == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {

                Column(
                    modifier = Modifier
                        .background(Color.White)
                        .padding(bottom = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Spacer(modifier = Modifier.height(16.dp))

                        SessionProgressBar(
                            currentStep = currentStep,
                            instructionText = if (isCreator) "Pantau status sesi." else "Cek status titipanmu.",
                            iconRes = icSesi,
                            onStepClick = { clickedStep ->
                                when (clickedStep) {
                                    1 -> { }
                                    2 -> {
                                        if (status != "open") onGoToShoppingList(session.id)
                                    }
//                                    3, 4 -> {
//                                        if (isCreator) {
//                                            onGoToShoppingList(session.id)
//                                        } else {
//                                            onGoToPayment(session.id)
//                                        }
                                    3, -> {
                                        if (status != "open" && status != "shopping") onGoToPayment(session.id)
                                    }
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        SessionInfoCard(
                            title = session.title,
                            description = session.description,
                            iconRes = R.drawable.ic_makanan,
                            durationSeconds = remainingSeconds.toInt(),
                            showTimer = true,
                            onChatClick = null
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    if (isCreator) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            TitipinTabItem("Menunggu (${viewModel.pendingCount})", isSelected = viewModel.selectedTabIndex == 0) { viewModel.selectedTabIndex = 0 }
                            TitipinTabItem("Diterima (${viewModel.acceptedCount})", isSelected = viewModel.selectedTabIndex == 1) { viewModel.selectedTabIndex = 1 }
                            TitipinTabItem("Ditolak (${viewModel.rejectedCount})", isSelected = viewModel.selectedTabIndex == 2) { viewModel.selectedTabIndex = 2 }
                        }
                        HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)
                    } else {
                        Text(
                            "Daftar Titipan Saya",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val listToShow = if (isCreator) {
                        viewModel.displayedOrdersForHost
                    } else {
                        orders.filter { it.requesterId == currentUser }
                    }

                    if (listToShow.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (isCreator) "Tidak ada permintaan di tab ini" else "Belum ada titipan.",
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
                }
            }
        }
    }
}

@Composable
fun AddOrderDialog(
    onDismiss: () -> Unit,
    onSubmit: () -> Unit,
    itemName: String, onNameChange: (String) -> Unit,
    quantity: Int, onQtyChange: (Int) -> Unit,
    price: String, onPriceChange: (String) -> Unit,
    notes: String, onNotesChange: (String) -> Unit,
    deliveryLocation: String, onLocationChange: (String) -> Unit,
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
                    value = deliveryLocation,
                    onValueChange = onLocationChange,
                    label = { Text("Lokasi Pengantaran (Gedung/Lantai)") },
                    placeholder = { Text("Cth: Gedung FTI Lt. 2") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = { Icon(Icons.Default.LocationOn, null) }
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
            Button(
                onClick = onSubmit,
                enabled = !isLoading && deliveryLocation.isNotBlank() && itemName.isNotBlank()
            ) {
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