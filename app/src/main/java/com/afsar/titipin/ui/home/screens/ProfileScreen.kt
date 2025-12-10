package com.afsar.titipin.ui.home.screens

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.afsar.titipin.ui.components.CirclesImage
import com.afsar.titipin.ui.components.ProfileMenuItem
import com.afsar.titipin.ui.circle.CircleActivity
import com.afsar.titipin.ui.theme.TextDarkSecondary
import com.afsar.titipin.ui.theme.TextLightPrimary
import com.afsar.titipin.ui.theme.jakartaFamily
import com.afsar.titipin.ui.home.viewmodel.ProfileViewModel


@Composable
fun ProfileScreen(viewModel: ProfileViewModel = hiltViewModel()) {
    val context = LocalContext.current
    var showBankAccountDialog by remember { mutableStateOf(false) }

    // Show success/error message
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
            fontFamily = jakartaFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = TextLightPrimary
        )
        
        val username = "@" + (viewModel.currentUser?.username ?: "")
        Text(
            text = username,
            fontFamily = jakartaFamily,
            fontWeight = FontWeight.Thin,
            fontSize = 18.sp,
            color = TextDarkSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Bank Account Card
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
                    Column {
                        Text(
                            text = "Rekening Bank",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        if (viewModel.currentUser?.bankAccountNumber?.isNotEmpty() == true) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = viewModel.currentUser?.bankName ?: "",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = viewModel.currentUser?.bankAccountNumber ?: "",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = viewModel.currentUser?.bankAccountName ?: "",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        } else {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Belum terdaftar",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                    TextButton(onClick = { showBankAccountDialog = true }) {
                        Text(if (viewModel.currentUser?.bankAccountNumber?.isNotEmpty() == true) "Ubah" else "Daftar")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Menu Items
        ProfileMenuItem(
            icon = Icons.Default.Group,
            text = "Circle Saya",
            onClick = {
                val intent = Intent(context, CircleActivity::class.java)
                context.startActivity(intent)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Logout Button
        Button(
            onClick = {
                viewModel.logout()
                val intent = Intent(context, com.afsar.titipin.ui.login.LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                context.startActivity(intent)
                (context as? android.app.Activity)?.finish()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
        ) {
            Text("Logout", color = Color.White)
        }
    }

    // Bank Account Dialog
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
        title = { Text("Daftar Rekening Bank") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = viewModel.bankName,
                    onValueChange = { viewModel.bankName = it },
                    label = { Text("Nama Bank") },
                    placeholder = { Text("Contoh: BCA, Mandiri, BNI") },
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
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = viewModel.bankAccountName,
                    onValueChange = { viewModel.bankAccountName = it },
                    label = { Text("Nama Pemilik Rekening") },
                    placeholder = { Text("Sesuai buku rekening") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Error message
                if (viewModel.errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = viewModel.errorMessage ?: "",
                        color = Color.Red,
                        fontSize = 12.sp
                    )
                }

                // Success message
                if (viewModel.bankAccountSaveSuccess == true) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "✅ Rekening berhasil disimpan!",
                        color = Color.Green,
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