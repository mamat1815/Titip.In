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
    onEditProfileClick: () -> Unit,      // Navigasi ke Edit Profil
    onPaymentOptionClick: () -> Unit,    // Navigasi ke Input Bank
    onCircleClick: () -> Unit,           // Navigasi ke List Circle
    onLogoutClick: () -> Unit,           // Navigasi Logout (ke Login Screen)
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val user = viewModel.currentUser
    val isLoading = viewModel.isLoading
    val bgColor = Color(0xFFF9FAFB)

    // REFRESH DATA SAAT HALAMAN DIBUKA (Penting setelah edit profil/bank)
    LaunchedEffect(Unit) {
        viewModel.fetchUserProfile()
    }

    Box(modifier = Modifier.fillMaxSize().background(bgColor)) {

        // 1. TAMPILKAN LOADING
        if (isLoading && user == null) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = OrangePrimary)
        }
        // 2. TAMPILKAN KONTEN
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

                    // === CARD UTAMA (INFO USER & STATS) ===
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp)) {

                        // Kotak Putih Dasar
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
                            modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                            shadowElevation = 4.dp
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(top = 60.dp, bottom = 24.dp, start = 16.dp, end = 16.dp)
                            ) {
                                // Nama & Username
                                Text(
                                    text = user.name.ifEmpty { "Tanpa Nama" },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = if (user.username.isNotEmpty()) "@${user.username}" else user.email,
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                // --- STATISTIK ROW (Titip, Sesi, Circle) ---
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    // Mengambil data dari user.stats (Model baru)
                                    StatItem(user.stats?.totalTitip ?: 0, "Total titip", Icons.Default.ShoppingBag)
                                    StatItem(user.stats?.totalSesi ?: 0, "Total sesi", Icons.Default.ShoppingCart)
                                    StatItem(user.stats?.totalCircle ?: 0, "Total circle", Icons.Default.SupervisedUserCircle)
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                // --- INFO KEUANGAN (Pemasukan & Pengeluaran) ---
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, Color(0xFFF5F5F5)),
                                    color = Color(0xFFFAFAFA),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Pemasukan (Income)
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Pemasukan", fontSize = 11.sp, color = Color.Gray)
                                            Text(
                                                text = viewModel.formatRupiah(user.stats?.totalIncome ?: 0.0),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = Color(0xFF2E7D32) // Hijau
                                            )
                                        }

                                        // Garis Pemisah Vertical
                                        Divider(
                                            modifier = Modifier
                                                .height(30.dp)
                                                .width(1.dp),
                                            color = Color.LightGray
                                        )
                                        Spacer(modifier = Modifier.width(16.dp))

                                        // Pengeluaran (Expense)
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Pengeluaran", fontSize = 11.sp, color = Color.Gray)
                                            Text(
                                                text = viewModel.formatRupiah(user.stats?.totalExpense ?: 0.0),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = Color(0xFFC62828) // Merah
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // --- FOTO PROFIL (Mengambang di atas) ---
                        CirclesImage(
                            imageUrl = user.photoUrl,
                            modifier = Modifier
                                .size(90.dp)
                                .align(Alignment.TopCenter)
                                .clip(CircleShape)
                                .border(4.dp, Color.White, CircleShape)
                                .background(Color.LightGray)
                        )

                        // --- TOMBOL EDIT (Icon Pensil) ---
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 50.dp, end = 20.dp)
                                .size(36.dp)
                                .background(Color.White, CircleShape)
                                .border(1.dp, Color(0xFFEEEEEE), CircleShape)
                                .clickable { onEditProfileClick() }, // Navigasi ke Edit
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Profil",
                                tint = OrangePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // === LIST MENU ===
                    Text("Pengaturan Umum", fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))

                    // 1. Opsi Pembayaran (Bank)
                    ProfileMenuItem(
                        icon = Icons.Default.AccountBalanceWallet,
                        title = "Opsi Pembayaran (Rekening)",
                        onClick = onPaymentOptionClick
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // 2. Circle Saya
                    ProfileMenuItem(
                        icon = Icons.Default.Group,
                        title = "Circle Saya",
                        onClick = onCircleClick
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // 3. Keluar / Logout
                    ProfileMenuItem(
                        icon = Icons.Default.ExitToApp,
                        title = "Keluar",
                        isDanger = true,
                        onClick = {
                            viewModel.logout()
                            onLogoutClick()
                        }
                    )

                    Spacer(modifier = Modifier.height(100.dp)) // Padding bawah agar tidak tertutup BottomBar
                }
            }
        } else {
            // STATE ERROR / GAGAL LOAD
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Gagal memuat profil.", color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.fetchUserProfile() },
                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                    ) {
                        Text("Coba Lagi")
                    }
                }
            }
        }
    }
}

// --- KOMPONEN PENDUKUNG ---

@Composable
fun StatItem(count: Int, label: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 11.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(count.toString(), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
        }
    }
}

@Composable
fun ProfileMenuItem(icon: ImageVector, title: String, isDanger: Boolean = false, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isDanger) Color.Red else Color.DarkGray
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Medium,
                color = if (isDanger) Color.Red else TextPrimary,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.LightGray
            )
        }
    }
}