package com.afsar.titipin.ui.components.molecules

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale

@Composable
fun UserBillCard(
    myBill: Double,       // Total Harga Barang
    myJastipFee: Double,  // Total Jasa Titip (Tip)
    myPaymentFee: Double, // Biaya Layanan / Admin
    myTotalWithFee: Double // Grand Total
) {
    // Formatter Rupiah
    val formatRp = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Text(
                text = "Rincian Biaya",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            // 1. Harga Barang
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Harga Barang", fontSize = 13.sp)
                Text(
                    text = formatRp.format(myBill),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }

            // 2. Jasa Titip (Tip) - Hanya muncul jika ada (> 0)
            if (myJastipFee > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Jasa Titip (Tip)", fontSize = 13.sp)
                    Text(
                        text = formatRp.format(myJastipFee),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }

            // 3. Biaya Layanan
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Biaya Layanan (2% + 2.500)",
                    fontSize = 12.sp,
                    color = Color(0xFFE65100) // Orange Gelap
                )
                Text(
                    text = formatRp.format(myPaymentFee),
                    fontSize = 12.sp,
                    color = Color(0xFFE65100)
                )
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // 4. Grand Total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total Bayar", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(
                    text = formatRp.format(myTotalWithFee),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF1976D2) // Biru
                )
            }
        }
    }
}