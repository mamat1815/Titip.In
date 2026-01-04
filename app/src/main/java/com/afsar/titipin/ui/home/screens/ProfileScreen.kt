package com.afsar.titipin.ui.home.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.afsar.titipin.ui.components.CirclesImage
import com.afsar.titipin.ui.home.viewmodel.ProfileViewModel
import com.afsar.titipin.ui.theme.OrangePrimary
import com.afsar.titipin.ui.theme.TextPrimary

@Composable
fun ProfileScreen(
//    onLogoutClick: () -> Unit, // Callback untuk navigasi logout
//    onCircleClick: () -> Unit, // Callback jika mau ke menu circle
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val user = viewModel.currentUser
    val isLoading = viewModel.isLoading
    val BgColor = Color(0xFFF9FAFB)

    Box(modifier = Modifier.fillMaxSize().background(BgColor)) {

        // 1. TAMPILKAN LOADING JIKA SEDANG MEMUAT
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
        // 2. CEK APAKAH USER NULL
        else if (user != null) {
            Column(modifier = Modifier.fillMaxSize()) {

                // HEADER
                Column(
                    modifier = Modifier
                        .background(Color.White)
                        .statusBarsPadding()
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Text("Profil", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = TextPrimary)
                }

                // KONTEN SCROLLABLE
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp)
                ) {
                    Spacer(modifier = Modifier.height(24.dp))

                    // === CARD UTAMA ===
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp)) {

                        // Kotak Putih di Bawah
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
                            modifier = Modifier.fillMaxWidth().padding(top = 40.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(top = 60.dp, bottom = 24.dp, start = 16.dp, end = 16.dp)
                            ) {
                                // Safe call untuk String
                                Text(user.name.ifEmpty { "Tanpa Nama" }, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text("@${user.username}", color = Color.Gray, fontSize = 14.sp)

                                Spacer(modifier = Modifier.height(24.dp))

                                // Stats Row (HINDARI !! GANTI DENGAN ?:)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                                    StatItem(user.stats?.totalTitip ?: 0, "Total titip", Icons.Default.ShoppingBag)
                                    StatItem(user.stats?.totalSesi ?: 0, "Total sesi", Icons.Default.ShoppingCart)
                                    StatItem(user.stats?.totalCircle ?: 0, "Total circle", Icons.Default.SupervisedUserCircle)
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                // Wallet Info (HINDARI !! GANTI DENGAN ?:)
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
                                    color = Color.White,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Pemasukan", fontSize = 10.sp, color = Color.Gray)
                                            Text(user.wallet?.income ?: "Rp 0", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        }
                                        Divider(modifier = Modifier.height(30.dp).width(1.dp))
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Pengeluaran", fontSize = 10.sp, color = Color.Gray)
                                            Text(user.wallet?.expense ?: "Rp 0", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        }
                                    }
                                }
                            }
                        }

                        // Avatar
                        CirclesImage(
                            imageUrl = user.photoUrl,
                            modifier = Modifier
                                .size(90.dp)
                                .align(Alignment.TopCenter)
                                .border(4.dp, Color.White, CircleShape)
                        )

                        // Edit Icon
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = OrangePrimary,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 56.dp, end = 16.dp)
                                .clickable {
                                    // Handle Edit Profile Navigation
                                }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // MENU LIST
                    Text("Pengaturan Umum", fontSize = 14.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))

                    ProfileMenuItem(Icons.Default.AccountBalanceWallet, "Opsi Pembayaran") {
                        // Handle Payment Options
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    ProfileMenuItem(Icons.Default.Group, "Circle Saya") {
//                        onCircleClick()
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    // TOMBOL KELUAR
                    ProfileMenuItem(Icons.Default.ExitToApp, "Keluar", isDanger = true) {
                        viewModel.logout() // Panggil fungsi di VM
//                        onLogoutClick()    // Panggil callback navigasi
                    }

                    Spacer(modifier = Modifier.height(100.dp)) // Padding bawah biar ga ketutup bottom bar
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Gagal memuat profil. Coba lagi.", color = Color.Gray)
                Button(onClick = { viewModel.fetchUserProfile() }, modifier = Modifier.padding(top=8.dp)) {
                    Text("Refresh")
                }
            }
        }
    }
}

// ... StatItem dan ProfileMenuItem SAMA seperti kodemu ...
@Composable
fun StatItem(count: Int, label: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 11.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(count.toString(), fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

@Composable
fun ProfileMenuItem(icon: ImageVector, title: String, isDanger: Boolean = false, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        modifier = Modifier.fillMaxWidth().height(56.dp).clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = if (isDanger) Color.Red else Color.Black)
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, fontWeight = FontWeight.Medium, color = if (isDanger) Color.Red else TextPrimary, modifier = Modifier.weight(1f))
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.LightGray)
        }
    }
}