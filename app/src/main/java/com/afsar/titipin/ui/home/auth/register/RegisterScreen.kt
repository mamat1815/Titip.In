package com.afsar.titipin.ui.home.auth.register

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.afsar.titipin.ui.components.atoms.InputTextField
import com.afsar.titipin.ui.components.atoms.PrimaryButton
import com.afsar.titipin.ui.theme.OrangePrimary
import com.afsar.titipin.ui.theme.TextPrimary
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.afsar.titipin.ui.components.molecules.LoadingOverlay


@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onLoginClick: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {

    LaunchedEffect(viewModel.isRegisterSuccess) {
        if (viewModel.isRegisterSuccess) {
            onRegisterSuccess()
        }
    }


    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp, top = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onLoginClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }

            Text(
                text = "Buat Akun",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            if (viewModel.errorMessage != null) {
                Text(
                    text = viewModel.errorMessage!!,
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            InputTextField(value = viewModel.nameInput, onValueChange = { viewModel.nameInput = it }, placeholder = "Nama Lengkap")
            Spacer(modifier = Modifier.height(16.dp))

            InputTextField(value = viewModel.usernameInput, onValueChange = { viewModel.usernameInput = it }, placeholder = "Username Lengkap")
            Spacer(modifier = Modifier.height(16.dp))
            InputTextField(value = viewModel.emailInput, onValueChange = { viewModel.emailInput = it }, placeholder = "Email")
            Spacer(modifier = Modifier.height(16.dp))

            InputTextField(value = viewModel.passwordInput, onValueChange = { viewModel.passwordInput = it }, placeholder = "Password", visualTransformation = PasswordVisualTransformation())
            Spacer(modifier = Modifier.height(16.dp))

            InputTextField(value = viewModel.confirmPasswordInput, onValueChange = { viewModel.confirmPasswordInput= it }, placeholder = "Konfirmasi Password", visualTransformation = PasswordVisualTransformation())
            Spacer(modifier = Modifier.height(32.dp))

            PrimaryButton(
                text = "Buat Akun",
                onClick = {
                    viewModel.onRegisterClicked()
                },
                enabled = !viewModel.isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Sudah punya akun?", color = Color.Gray, fontSize = 14.sp)
                TextButton(
                    onClick = onLoginClick,
                    modifier = Modifier.offset(x = (-10).dp)
                ) {
                    Text("Masuk", color = OrangePrimary, fontWeight = FontWeight.Bold)
                }
                LoadingOverlay(isLoading = viewModel.isLoading)
            }
        }

        if (viewModel.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = OrangePrimary)
            }
        }
    }
}