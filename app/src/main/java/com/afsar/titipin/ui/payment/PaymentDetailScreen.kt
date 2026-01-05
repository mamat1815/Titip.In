package com.afsar.titipin.ui.payment

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.afsar.titipin.R
import com.afsar.titipin.ui.components.molecules.SessionProgressBar
import com.afsar.titipin.ui.components.molecules.UserBillCard
import com.afsar.titipin.ui.theme.OrangePrimary
import java.text.NumberFormat
import java.util.Locale



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentDetailScreen(
    onBackClick: () -> Unit,
    onGoToDetailSession: (String) -> Unit,
    onGoToShoppingList: (String) -> Unit,
    viewModel: PaymentDetailViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val formatRp = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    val isCreator = viewModel.isCreator
    val isPaid = viewModel.isPaid

    val paymentStatus = viewModel.myPaymentStatus
    val sessionStatus = viewModel.sessionStatus

    val currentProgressStep = if (sessionStatus == "closed") 4 else 3

    val instructionText = if (isCreator) {
        if (currentProgressStep == 4) "Sesi selesai. Dana berhasil dikelola."
        else "Cek rincian pendapatan & cairkan dana."
    } else {
        if (isPaid) "Pembayaran berhasil diverifikasi."
        else "Segera lunasi tagihanmu."
    }

    if (viewModel.disbursementMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearDisbursementMessage() },
            confirmButton = { TextButton(onClick = { viewModel.clearDisbursementMessage() }) { Text("OK") } },
            title = {
                Text(
                    text = if (viewModel.disbursementStatus == "success") "Status Pencairan" else "Info",
                    fontWeight = FontWeight.Bold
                )
            },
            text = { Text(viewModel.disbursementMessage!!) }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isCreator) "Rincian Pendapatan" else "Rincian Pembayaran",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Surface(
                shadowElevation = 16.dp,
                color = Color.White,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isCreator) "Total Bersih Diterima" else "Total Tagihan",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = formatRp.format(if (isCreator) viewModel.netDisbursement else viewModel.myGrandTotal),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = if (isCreator) Color(0xFF2E7D32) else OrangePrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isCreator) {
                        // --- TOMBOL HOST ---
                        Button(
                            onClick = { viewModel.requestDisbursement() },
                            // SECURITY UI: Tombol mati jika isLoading OR saldo <= 0 OR sudah success OR sedang loading
                            enabled = !viewModel.isLoading && viewModel.canDisburse && viewModel.disbursementStatus != "loading" && viewModel.disbursementStatus != "success",
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1976D2), // Biru
                                disabledContainerColor = Color.LightGray
                            ),
                            shape = RoundedCornerShape(100)
                        ) {
                            if (viewModel.disbursementStatus == "loading") {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                            } else if (viewModel.disbursementStatus == "success") {
                                Text("Dana Sudah Dicairkan ✅", fontWeight = FontWeight.Bold)
                            } else {
                                Icon(Icons.Default.MonetizationOn, null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Cairkan Dana ke Rekening", fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        // --- TOMBOL GUEST ---
                        if (isPaid) {
                            Button(
                                onClick = { },
                                enabled = false,
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                colors = ButtonDefaults.buttonColors(
                                    disabledContainerColor = Color(0xFFE8F5E9),
                                    disabledContentColor = Color(0xFF2E7D32)
                                ),
                                shape = RoundedCornerShape(100)
                            ) {
                                Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Lunas / Sudah Dibayar", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Button(
                                onClick = {
                                    val intent = Intent(context, PaymentActivity::class.java).apply {
                                        putExtra(PaymentActivity.EXTRA_SESSION_ID, viewModel.sessionId)
                                        putExtra(PaymentActivity.EXTRA_USER_ID, viewModel.currentUserId)
                                        putExtra(PaymentActivity.EXTRA_AMOUNT, viewModel.mySubTotal.toLong())
                                        putExtra(PaymentActivity.EXTRA_USER_NAME, viewModel.currentUserName)
                                        putExtra(PaymentActivity.EXTRA_USER_EMAIL, viewModel.currentUserEmail)
                                        putExtra(PaymentActivity.EXTRA_USER_PHONE, viewModel.currentUserPhone)
                                    }
                                    context.startActivity(intent)
                                },
                                enabled = !viewModel.isLoading && viewModel.myGrandTotal > 0 && paymentStatus != "pending",
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (paymentStatus == "pending") Color(0xFFFFA000) else OrangePrimary
                                ),
                                shape = RoundedCornerShape(100)
                            ) {
                                if (paymentStatus == "pending") {
                                    Text("Lanjutkan Pembayaran (Pending)", fontWeight = FontWeight.Bold)
                                } else {
                                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Bayar Sekarang", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFFF9FAFB)
    ) { padding ->

        if (viewModel.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = OrangePrimary)
            }
        } else if (viewModel.errorMessage != null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(text = viewModel.errorMessage!!, color = Color.Red)
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    SessionProgressBar(
                        currentStep = currentProgressStep,
                        instructionText = instructionText,
                        iconRes = if (currentProgressStep == 4) R.drawable.ic_delivered else R.drawable.ic_delivery,
                        onStepClick = { clickedStep ->
                            when (clickedStep) {
                                1 -> {
                                    onGoToDetailSession(viewModel.sessionId)
                                }
                                2 -> {
                                    if (viewModel.sessionStatus != "open") onGoToShoppingList(viewModel.sessionId)
                                }
                                3 -> {
//                                    if (isCreator) {
//                                        onGoToShoppingList(viewModel.sessionId)
//                                    } else {
//                                        onGoToPayment(viewModel.sessionId)
//                                    }
                                }
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                item {
                    if (isCreator) {
                        DisbursementInfoCard(
                            totalCollected = viewModel.totalCollected,
                            fee = viewModel.disbursementFee,
                            net = viewModel.netDisbursement
                        )
                    } else {
                        Surface(
                            color = Color(0xFFFFF3E0),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lock, null, tint = OrangePrimary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Pembayaran aman & otomatis diverifikasi.", fontSize = 12.sp, color = Color.DarkGray)
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = if (isCreator) "Rincian Titipan (Semua User)" else "Barang yang dibayar",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                if (viewModel.orders.isEmpty()) {
                    item { Text("Tidak ada data.", color = Color.Gray, fontSize = 14.sp) }
                } else {
                    items(viewModel.orders) { order ->
                        if (isCreator) {
                            Text(
                                text = "Pemesan: ${order.requesterName}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = OrangePrimary,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        order.items.forEach { item ->
                            PaymentItemRow(name = item.name, qty = item.quantity, price = item.priceEstimate)
                            HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    if (isCreator) {
                    } else {
                        UserBillCard(
                            myBill = viewModel.myTotalGoodsPrice,
                            myJastipFee = viewModel.myJastipFee,
                            myPaymentFee = viewModel.myAdminFee,
                            myTotalWithFee = viewModel.myGrandTotal
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(20.dp)) }
            }
        }
    }
}

@Composable
fun DisbursementInfoCard(
    totalCollected: Double,
    fee: Double,
    net: Double
) {
    val formatRp = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Estimasi Pencairan", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1565C0))
            Spacer(modifier = Modifier.height(8.dp))

            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text("Total Pendapatan", fontSize = 13.sp)
                Text(formatRp.format(totalCollected), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text("Biaya Admin Bank", fontSize = 13.sp, color = Color(0xFFE65100))
                Text("- ${formatRp.format(fee)}", fontSize = 13.sp, color = Color(0xFFE65100))
            }
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text("Bersih Diterima", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(formatRp.format(net), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF2E7D32))
            }
        }
    }
}

@Composable
fun PaymentItemRow(
    name: String,
    qty: Int,
    price: Double
) {
    val formatRp = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .background(Color.White),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(name, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Color.Black)
            Text("$qty x ${formatRp.format(price)}", fontSize = 12.sp, color = Color.Gray)
        }
        Text(
            text = formatRp.format(price * qty),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color.Black
        )
    }
}