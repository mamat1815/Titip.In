package com.afsar.titipin.ui.home.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.*
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.afsar.titipin.ui.home.viewmodel.EditProfileViewModel
import com.afsar.titipin.ui.home.viewmodel.PaymentOptionViewModel
import com.afsar.titipin.ui.theme.OrangePrimary


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentOptionScreen(
    onBackClick: () -> Unit,
    viewModel: PaymentOptionViewModel = hiltViewModel()
) {
    if (viewModel.uiMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearMessage() },
            confirmButton = { TextButton(onClick = { viewModel.clearMessage() }) { Text("OK") } },
            text = { Text(viewModel.uiMessage!!) }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Opsi Pembayaran") },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, null, tint = Color(0xFF1565C0))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Rekening ini akan digunakan untuk menerima pencairan dana dari sesi yang kamu buat (Host).",
                        fontSize = 12.sp, color = Color(0xFF1565C0)
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            Text("Informasi Bank", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(16.dp))

            // Bank Name (Bisa diganti Dropdown menu)
            OutlinedTextField(
                value = viewModel.bankName,
                onValueChange = { viewModel.bankName = it.uppercase() },
                label = { Text("Nama Bank (BCA, MANDIRI, dll)") },
                placeholder = { Text("BCA") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = viewModel.accountNumber,
                onValueChange = { if (it.all { char -> char.isDigit() }) viewModel.accountNumber = it },
                label = { Text("Nomor Rekening") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = viewModel.accountName,
                onValueChange = { viewModel.accountName = it },
                label = { Text("Atas Nama (Sesuai Buku Tabungan)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { viewModel.saveBank() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                enabled = !viewModel.isLoading
            ) {
                if (viewModel.isLoading) CircularProgressIndicator(color = Color.White)
                else Text("Simpan Rekening")
            }
        }
    }
}