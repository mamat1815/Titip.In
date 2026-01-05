package com.afsar.titipin.ui.payment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale

@Composable
fun PaymentStatusCard(
    amount: Double,
    status: String,
    onPayClick: (() -> Unit)? = null, // Callback null jika status sukses
    modifier: Modifier = Modifier
) {
    val formatRp = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

    // Tentukan Warna, Icon, dan Teks berdasarkan Status Midtrans
    val (backgroundColor, textColor, icon, statusText) = when (status.lowercase()) {
        "success", "settlement", "capture" -> PaymentStatusAttr(
            Color(0xFFE8F5E9), Color(0xFF2E7D32), Icons.Default.CheckCircle, "Lunas"
        )
        "pending" -> PaymentStatusAttr(
            Color(0xFFFFF3E0), Color(0xFFE65100), Icons.Default.HourglassEmpty, "Menunggu Pembayaran"
        )
        "failed", "deny", "cancel" -> PaymentStatusAttr(
            Color(0xFFFFEBEE), Color(0xFFC62828), Icons.Default.Error, "Gagal"
        )
        "expire" -> PaymentStatusAttr(
            Color(0xFFF5F5F5), Color(0xFF616161), Icons.Default.Cancel, "Kadaluarsa"
        )
        else -> PaymentStatusAttr( // Default / Belum Bayar
            Color(0xFFF5F7FA), Color(0xFF616161), Icons.Default.HourglassEmpty, "Belum Dibayar"
        )
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Baris Atas: Label & Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total Tagihan",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = formatRp.format(amount),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }

                Icon(
                    imageVector = icon,
                    contentDescription = statusText,
                    tint = textColor,
                    modifier = Modifier.size(32.dp)
                )
            }

            // Baris Tengah: Status Text
            Text(
                text = statusText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
            )

            // Baris Bawah: Tombol Bayar (Hanya jika belum sukses)
            // Tampilkan tombol jika status pending, failed, atau kosong/belum bayar
            if (status.lowercase() !in listOf("success", "settlement", "capture") && onPayClick != null) {
                Button(
                    onClick = onPayClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1976D2) // Biru Midtrans
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Bayar Sekarang", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// Helper Class agar kode when lebih rapi
private data class PaymentStatusAttr(
    val bg: Color,
    val text: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val label: String
)