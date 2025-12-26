package com.afsar.titipin.ui.home.auth.login

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.afsar.titipin.R
import com.afsar.titipin.ui.components.atoms.InputTextField
import com.afsar.titipin.ui.components.atoms.PrimaryButton
//import com.afsar.titipin.ui.home.auth.register.OrSeparator
import com.afsar.titipin.ui.theme.OrangePrimary
import com.afsar.titipin.ui.theme.Primary
import com.afsar.titipin.ui.theme.TextDarkSecondary
import com.afsar.titipin.ui.theme.TextLightPrimary
import com.afsar.titipin.ui.theme.TextPrimary
import com.afsar.titipin.ui.theme.jakartaFamily
import com.afsar.titipin.ui.components.molecules.LoadingOverlay
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

//@Composable
//fun LoginScreens(onLoginSuccess: () -> Unit,
//                onNavigateToRegister: () -> Unit,
//                viewModel: LoginViewModel = hiltViewModel()) {
//    val context = LocalContext.current
//    LaunchedEffect(viewModel.isLoginSuccess) {
//        if (viewModel.isLoginSuccess) {
////            val intent = Intent(context, MainActivity::class.java)
////            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
////            context.startActivity(intent)
////
////            (context as? Activity)?.finish()
//            onLoginSuccess()
//        }
//    }
//
//    val googleSignInLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.StartActivityForResult()
//    ) { result ->
//        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
//        viewModel.onGoogleSignInResult(task)
//    }
//
//    fun launchGoogleSignIn() {
////        TODO PINDAH KE LOCAL PROPS
//        val webClientId = "212927678912-b9njd6313nb1h8vmqm6tg9tuq8u5hcc0.apps.googleusercontent.com"
//        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//            .requestIdToken(webClientId)
//            .requestEmail()
//            .build()
//        val googleSignInClient = GoogleSignIn.getClient(context, gso)
//        googleSignInLauncher.launch(googleSignInClient.signInIntent)
//    }
//
//    Column (
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp)
//    ){
//
//        Spacer(
//            modifier = Modifier.height(32.dp)
//        )
//
//        Text(
//            text = "Selamat Datang Kembali",
//            fontFamily = jakartaFamily,
//            fontWeight = FontWeight.Bold,
//            fontSize = 24.sp,
//            color = TextLightPrimary
//        )
//
//        Text(
//            text = "Masuk untuk melanjutkan ke akun anda",
//            fontFamily = jakartaFamily,
//            fontWeight = FontWeight.Thin,
//            fontSize = 12.sp,
//            color = TextLightPrimary
//        )
//
//        Spacer(
//            modifier = Modifier.height(16.dp)
//        )
//
//
//        Text(
//            text = "Email",
//            fontFamily = jakartaFamily,
//            fontWeight = FontWeight.SemiBold,
//            fontSize = 16.sp,
//            color = TextLightPrimary
//        )
//        OutlinedTextField(
//            value = viewModel.emailInput,
//            modifier = Modifier.fillMaxWidth(),
//            onValueChange = {viewModel.emailInput = it},
//            label = { Text("Masukkan Alamat Email") }
//        )
//
//        Spacer(
//            modifier = Modifier.height(8.dp)
//        )
//
//        Text(
//            text = "Password",
//            fontFamily = jakartaFamily,
//            fontWeight = FontWeight.SemiBold,
//            fontSize = 16.sp,
//            color = TextLightPrimary
//        )
//        var isPasswordVisible by remember { mutableStateOf(false) }
//
//        OutlinedTextField(
//            value = viewModel.passwordInput,
//            onValueChange = {
//                viewModel.passwordInput = it
//            },
//            label = { Text("Masukkan Password") },
//            modifier = Modifier.fillMaxWidth(),
//            singleLine = true,
//
//            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
//
//            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
//
//            trailingIcon = {
//                val image = if (isPasswordVisible)
//                    Icons.Filled.Visibility
//                else
//                    Icons.Filled.VisibilityOff
//
//                val description = if (isPasswordVisible) "Hide password" else "Show password"
//
//                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
//                    Icon(imageVector = image, contentDescription = description)
//                }
//            }
//        )
//
//
//        Text(
//            text = "Lupa Password?",
//            color = Primary,
//            fontSize = 14.sp,
//            fontWeight = FontWeight.Bold,
//            modifier = Modifier
//                .align(Alignment.End)
//                .padding(top = 8.dp, end = 4.dp)
//                .clickable {
//                    Toast.makeText(context, "Login Clicked", Toast.LENGTH_SHORT).show()
//                }
//        )
//        Spacer(
//            modifier = Modifier.height(8.dp)
//        )
//        Button(
//            modifier = Modifier.fillMaxWidth(),
//            enabled = !viewModel.isLoading,
//            onClick = {
//                viewModel.onLoginClicked()
//            },
//        ) {
//
//            if (viewModel.isLoading){
//                Text(
//                    text = "Loading...",
//                    fontSize = 18.sp,
//                    fontFamily = jakartaFamily,
//                    fontWeight = FontWeight.Bold,
//                    color = Color.White
//                )
//            } else {
//                Text(
//                    text = "Masuk",
//                    fontSize = 18.sp,
//                    fontFamily = jakartaFamily,
//                    fontWeight = FontWeight.Bold,
//                    color = Color.White
//                )
//            }
//
//        }
//        Spacer(
//            modifier = Modifier.height(8.dp)
//        )
//        OrSeparator()
//        Spacer(
//            modifier = Modifier.height(16.dp)
//        )
//
//        OutlinedButton(
//            onClick = {
//                launchGoogleSignIn()
//            },
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(56.dp),
//            shape = RoundedCornerShape(16.dp),
//            border = BorderStroke(1.dp, TextDarkSecondary),
//            colors = ButtonDefaults.outlinedButtonColors(
//                containerColor = Color.White
//            )
//        ) {
//            Row(
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.Center
//            ) {
//                Image(
//                    painter = painterResource(id = R.drawable.ic_google),
//                    contentDescription = "Google Logo",
//                    modifier = Modifier.size(24.dp)
//                )
//
//                Spacer(modifier = Modifier.width(12.dp))
//
//                Text(
//                    text = "Lanjutkan dengan Google",
//                    fontSize = 16.sp,
//                    color = Color.Black,
//                    fontWeight = FontWeight.Medium
//                )
//            }
//        }
//
//        Spacer(
//            modifier = Modifier.height(16.dp)
//        )
//
//        val annotatedText = buildAnnotatedString {
//            append("Belum punya akun? ")
//            withStyle(
//                style = SpanStyle(
//                    color = Primary,
//                    fontWeight = FontWeight.Bold
//                )
//            ) {
//                append("Register di sini")
//            }
//        }
//
//        Text(
//            text = annotatedText,
//            fontSize = 14.sp,
//            textAlign = TextAlign.Center,
//            modifier = Modifier.clickable(
//                interactionSource = remember { MutableInteractionSource() },
//                indication = null,
//                onClick = {
////                    val intent = Intent(context, RegisterActivity::class.java)
////                    context.startActivity(intent)
//                    onNavigateToRegister()
//                }
//
//            )
//                .fillMaxWidth()
//        )
//
//    }
//}

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {

    val context = LocalContext.current

    LaunchedEffect(viewModel.isLoginSuccess) {
        if (viewModel.isLoginSuccess) {
            onLoginSuccess()
        }
    }


//TODO Bisa dipindah di viewmodel

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        viewModel.onGoogleSignInResult(task)
    }

    fun launchGoogleSignIn() {
//        TODO PINDAH KE LOCAL PROPS
        val webClientId = "212927678912-b9njd6313nb1h8vmqm6tg9tuq8u5hcc0.apps.googleusercontent.com"
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(context, gso)
        googleSignInLauncher.launch(googleSignInClient.signInIntent)
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
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }

            Text(
                text = "Masuk",
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

            InputTextField(
                value = viewModel.emailInput,
                onValueChange = { viewModel.emailInput = it },
                placeholder = "Email"
            )

            Spacer(modifier = Modifier.height(16.dp))

            InputTextField(
                value = viewModel.passwordInput,
                onValueChange = { viewModel.passwordInput = it },
                placeholder = "Password",
                visualTransformation = PasswordVisualTransformation()
            )

            Spacer(modifier = Modifier.height(32.dp))

            PrimaryButton(
                text = "Masuk",
                onClick = { viewModel.onLoginClicked() },
                enabled = !viewModel.isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Button(
                onClick = { launchGoogleSignIn() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black,
                    disabledContainerColor = Color.Gray,
                    disabledContentColor = Color.Blue
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_google),
                        contentDescription = "Google Logo",
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "Lanjutkan dengan Google",
                        fontSize = 16.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Belum punya akun?", color = Color.Gray, fontSize = 14.sp)
                TextButton(
                    onClick = onRegisterClick,
                    modifier = Modifier.offset(x = (-10).dp)
                ) {
                    Text("Buat Akun", color = OrangePrimary, fontWeight = FontWeight.Bold)
                }
                LoadingOverlay(isLoading = viewModel.isLoading)
            }
        }

        if (viewModel.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = OrangePrimary)
            }
        }
    }
}