package com.afsar.titipin.ui.home.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.afsar.titipin.ui.components.CirclesImage
import com.afsar.titipin.ui.components.ProfileMenuItem
import com.afsar.titipin.ui.theme.TextDarkSecondary
import com.afsar.titipin.ui.theme.TextLightPrimary
import com.afsar.titipin.ui.home.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onLogoutClick: () -> Unit,
    onCircleClick: () -> Unit // Callback navigasi
) {
    val context = LocalContext.current
    var showBankAccountDialog by remember { mutableStateOf(false) }

    // Tutup dialog otomatis jika sukses simpan
    LaunchedEffect(viewModel.bankAccountSaveSuccess) {
        if (viewModel.bankAccountSaveSuccess == true) {
            showBankAccountDialog = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        CirclesImage(
            imageUrl = viewModel.currentUser?.photoUrl,
            size = 120.dp,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = viewModel.currentUser?.name ?: "Loading...",
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = TextLightPrimary
        )

        val username = "@" + (viewModel.currentUser?.username ?: "")
        Text(
            text = username,
            fontWeight = FontWeight.Thin,
            fontSize = 18.sp,
            color = TextDarkSecondary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- KARTU REKENING BANK ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F7FA))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Rekening Penerimaan (Jastip)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        val bank = viewModel.currentUser?.bank
                        if (bank != null && bank.bankAccountNumber.isNotEmpty()) {
                            Text(
                                text = "${bank.bankName} (${bank.bankCode})", // Tampilkan kode juga
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF370061)
                            )
                            Text(
                                text = bank.bankAccountNumber,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "a.n. ${bank.bankAccountName}",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        } else {
                            Text(
                                text = "Belum ada rekening terdaftar. Anda tidak bisa mencairkan dana jastip.",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                    TextButton(onClick = { showBankAccountDialog = true }) {
                        Text(if (viewModel.currentUser?.bank?.bankAccountNumber?.isNotEmpty() == true) "Ubah" else "Daftar")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        ProfileMenuItem(
            icon = Icons.Default.Group,
            text = "Circle Saya",
            onClick = onCircleClick
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onLogoutClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
        ) {
            Text("Logout", color = Color.White)
        }

        Spacer(modifier = Modifier.height(50.dp))
    }

    if (showBankAccountDialog) {
        BankAccountDialog(
            viewModel = viewModel,
            onDismiss = {
                showBankAccountDialog = false
                viewModel.clearBankAccountMessage()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankAccountDialog(
    viewModel: ProfileViewModel,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.AccountBalance, contentDescription = null) },
        title = { Text("Rekening Pencairan Dana") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {

                // Input Kode Bank (Penting untuk Midtrans)
                OutlinedTextField(
                    value = viewModel.bankCode,
                    onValueChange = { viewModel.bankCode = it },
                    label = { Text("Kode Bank (ex: bca, bri)") },
                    placeholder = { Text("bca") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = viewModel.bankName,
                    onValueChange = { viewModel.bankName = it },
                    label = { Text("Nama Bank Lengkap") },
                    placeholder = { Text("Bank Central Asia") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = viewModel.bankAccountNumber,
                    onValueChange = { viewModel.bankAccountNumber = it },
                    label = { Text("Nomor Rekening") },
                    placeholder = { Text("1234567890") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = viewModel.bankAccountName,
                    onValueChange = { viewModel.bankAccountName = it },
                    label = { Text("Nama Pemilik Rekening") },
                    placeholder = { Text("Sesuai buku tabungan") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (viewModel.errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = viewModel.errorMessage ?: "",
                        color = Color.Red,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { viewModel.updateBankAccount() },
                enabled = !viewModel.isSavingBankAccount
            ) {
                if (viewModel.isSavingBankAccount) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                } else {
                    Text("Simpan")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}