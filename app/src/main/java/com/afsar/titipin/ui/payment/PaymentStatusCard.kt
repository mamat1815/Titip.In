@file:Suppress("DEPRECATION")

package com.afsar.titipin.ui.payment

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Cancel
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
    onPayClick: (() -> Unit)? = null,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
) {
    val formatRp = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    
    val (backgroundColor, textColor, icon, statusText) = when (status.lowercase()) {
        "success", "settlement" -> Tuple4(
            Color(0xFFE8F5E9), 
            Color(0xFF2E7D32),
            Icons.Default.CheckCircle,
            "✅ Sudah Dibayar"
        )
        "pending" -> Tuple4(
            Color(0xFFFFF3E0),
            Color(0xFFE65100),
            Icons.Default.HourglassEmpty,
            "⏳ Menunggu Pembayaran"
        )
        "failed", "deny", "cancel" -> Tuple4(
            Color(0xFFFFEBEE),
            Color(0xFFC62828),
            Icons.Default.Error,
            "❌ Pembayaran Gagal"
        )
        "expired" -> Tuple4(
            Color(0xFFF5F5F5),
            Color(0xFF616161),
            Icons.Default.Cancel,
            "Kadaluarsa"
        )
        else -> Tuple4(
            Color(0xFFF5F7FA),
            Color(0xFF616161),
            Icons.Default.HourglassEmpty,
            "Belum Dibayar"
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
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = statusText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = textColor
                )
            }
            
            // Show payment button if pending or failed and callback provided
            if ((status == "pending" || status == "failed" || status == "") && onPayClick != null) {
                Button(
                    onClick = onPayClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3)
                    )
                ) {
                    Text("Bayar Sekarang")
                }
            }
        }
    }
}

private data class Tuple4<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
