import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.afsar.titipin.data.local.DailyIncome
import com.afsar.titipin.ui.components.CirclesImage
import com.afsar.titipin.ui.theme.BackgroundLight
import com.afsar.titipin.ui.theme.Primary
import com.afsar.titipin.ui.theme.jakartaFamily
import com.afsar.titipin.ui.home.viewmodel.HomeViewModel
import com.afsar.titipin.ui.session.SessionActivity
import kotlin.jvm.java

@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
//    val scrollState = rememberScrollState()
    val context = LocalContext.current
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        val intent = Intent(context, SessionActivity::class.java)
                        context.startActivity(intent)
                    },
                    containerColor = Primary
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = Color.White

                    )
                }
            }
        ){ innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundLight)
//                    .verticalScroll(scrollState)
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),


            ) {

                Row (
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ){

                    CirclesImage(
                        imageUrl = viewModel.currentUser?.photoUrl,
                        size = 40.dp,
                        modifier = Modifier.padding(end = 8.dp)
                    )

                    Spacer(Modifier.width(8.dp))
// TODO auth
                    val hello = viewModel.currentUser?.name
                    Text(
                        text = "Halo, $hello",
                        fontFamily = jakartaFamily,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }

                Text(
                    text = "Beranda",
                    fontSize = 28.sp,
                    fontFamily = jakartaFamily,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(vertical = 20.dp)
                )

                // 2. Card Sesi Titipan Saat Ini
                ActiveSessionCard()

                Spacer(modifier = Modifier.height(20.dp))

                // 3. Card Pendapatan Mingguan
                WeeklyIncomeCard()

                Spacer(modifier = Modifier.height(24.dp))

                // 4. Riwayat Titipan
                HistorySection()

                // Spacer bawah agar konten tidak tertutup FAB/Nav
                Spacer(modifier = Modifier.height(100.dp))
            }

        }

}

@Composable
fun ActiveSessionCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sesi Titipan Saat Ini",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = jakartaFamily

                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Timer,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
//                        TODO Timer
                        text = "01:30:07",
                        fontSize = 16.sp,
                        color = Primary,
                        fontWeight = FontWeight.Bold,
                        fontFamily = jakartaFamily
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
//                        TODO title
            Text(
                text = "Titip Jajan di Kantin",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = jakartaFamily
            )
            //                        TODO desc
            Text(
                text = "Ayo yang mau nitip makan siang, ditunggu sampai jam 12 siang ya!",
                fontSize = 12.sp,
                color = Primary,
                lineHeight = 16.sp,
                modifier = Modifier.padding(top = 4.dp),
                fontFamily = jakartaFamily
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Footer Card (Avatar Pile & Button)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mock FacePile (Avatar bertumpuk)
                Row(horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
                    repeat(3) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray)
                                .padding(1.dp) // Border effect
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("+2", fontSize = 10.sp, fontWeight = FontWeight.Bold,
                            fontFamily = jakartaFamily)
                    }
                }

                TextButton(onClick = { /* TODO ke halaman detail berdasarkan */ }) {
                    Text("Lihat Detail",
                        color = Primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        fontFamily = jakartaFamily)
                }
            }
        }
    }
}

@Composable
fun WeeklyIncomeCard() {
    val data = listOf(
        DailyIncome("S", 0.4f),
        DailyIncome("S", 0.3f),
        DailyIncome("R", 0.6f),
        DailyIncome("K", 0.5f),
        DailyIncome("J", 0.8f),
        DailyIncome("S", 0.2f),
        DailyIncome("M", 0.9f)
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Pendapatan Mingguan", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(16.dp))

            // Bar Chart Custom
            Row(
                modifier = Modifier.fillMaxWidth().height(100.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                data.forEach { item ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // Bar
                        Box(
                            modifier = Modifier
                                .width(12.dp)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(50))
                                .background(Color(0xFFF0E6FF)), // Background bar pudar
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(item.percentage) // Tinggi sesuai data
                                    .clip(RoundedCornerShape(50))
                                    .background(if (item.percentage > 0.7f) Color(0xFFB39DDB) else Color(0xFFD1C4E9)) // Warna ungu beda dikit
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = item.day, fontSize = 10.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun HistorySection() {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
//            Nanti buat halaman riwayat
            Text("Riwayat Titipan", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("Lihat Semua", color = Primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))
//            Todo itemlazy
        // List Item
        HistoryItem(
            title = "Titip Martabak Manis",
            date = "05 Oktober 2023",
            price = "Rp 60.000",
            icon = Icons.Default.Fastfood
        )
        Spacer(modifier = Modifier.height(8.dp))
        HistoryItem(
            title = "Beli Kopi Susu",
            date = "04 Oktober 2023",
            price = "Rp 22.000",
            icon = Icons.Default.LocalCafe
        )
    }
}

@Composable
fun HistoryItem(title: String, date: String, price: String, icon: ImageVector) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color.Black)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(date, fontSize = 12.sp, color = Color.Gray)
            }

            Text(price, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}