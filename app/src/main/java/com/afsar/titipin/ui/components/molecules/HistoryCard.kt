package com.afsar.titipin.ui.components.molecules

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.afsar.titipin.R
import com.afsar.titipin.data.model.Category
import com.afsar.titipin.data.model.Order
import com.afsar.titipin.data.model.Session
import com.afsar.titipin.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@Composable
fun OrdersItem(history: Order) {
    // Icon dummy (karena Order tidak simpan kategori, bisa ambil default atau join session)
    val iconRes = R.drawable.ic_belanja

    // Format Nama Barang (Gabungkan semua item jadi satu string)
    val itemNames = history.items.joinToString(", ") { "${it.quantity}x ${it.name}" }

    // Format Harga
    val formatRp = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    val totalPriceStr = formatRp.format(history.totalEstimate)

    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = itemNames.ifEmpty { "Barang dihapus" },
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Tampilkan status sebagai subtitle
                    val statusText = when(history.status) {
                        "pending" -> "Menunggu Konfirmasi"
                        "accepted" -> "Diterima Jastiper"
                        "bought" -> "Sudah Dibeli"
                        "rejected" -> "Ditolak"
                        else -> history.status
                    }

                    Text(
                        text = statusText,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            Text(
                text = totalPriceStr,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                fontSize = 14.sp
            )
        }
    }
}
@Composable
fun SessionsItem(history: Session) {
    val iconRes = when (history.category) {
        Category.FOOD -> R.drawable.ic_makanan
        Category.MEDICINE -> R.drawable.ic_obat
        Category.SHOPPING -> R.drawable.ic_belanja
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = history.locationName,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 14.sp
                    )
                    Text(
                        text = history.createdAt.toString(),
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

//            Text(
//                text = history.totalOmzet.toString(),
//                fontWeight = FontWeight.Bold,
//                color = TextPrimary,
//                fontSize = 14.sp
//            )
        }
    }
}