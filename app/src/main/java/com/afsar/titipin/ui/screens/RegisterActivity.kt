package com.afsar.titipin.ui.screens

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.afsar.titipin.R
import com.afsar.titipin.ui.home.MainActivity
import com.afsar.titipin.ui.theme.Primary
import com.afsar.titipin.ui.theme.TextDarkSecondary
import com.afsar.titipin.ui.theme.TextLightPrimary
import com.afsar.titipin.ui.theme.TitipInTheme
import com.afsar.titipin.ui.theme.jakartaFamily
import com.afsar.titipin.ui.viewmodel.RegisterViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn

import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TitipInTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    RegisterScreen(

                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun RegisterScreen(modifier: Modifier = Modifier, viewModel: RegisterViewModel = hiltViewModel()) {
    val context = LocalContext.current
    LaunchedEffect(viewModel.isRegisterSuccess) {
        if (viewModel.isRegisterSuccess) {
            val intent = Intent(context, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            context.startActivity(intent)
            (context as? Activity)?.finish()
        }
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        viewModel.onGoogleSignInResult(task)
    }

    fun launchGoogleSignIn() {
        val webClientId = "212927678912-b9njd6313nb1h8vmqm6tg9tuq8u5hcc0.apps.googleusercontent.com"
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId).requestEmail().build()
        val googleSignInClient = GoogleSignIn.getClient(context, gso)
        googleSignInLauncher.launch(googleSignInClient.signInIntent)
    }

    Column (
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ){
        Text(
            text = "Buat Akun Baru",
            fontFamily = jakartaFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = Primary
        )
        Spacer(
                modifier = Modifier.height(8.dp)
                )
        Text(
            text = "Daftarkan dirimu untuk mulai menitip barang dengan mudah",
            fontFamily = jakartaFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            color = TextLightPrimary
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Text(
            text = "Nama Lengkap",
            fontFamily = jakartaFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = TextLightPrimary
        )
        OutlinedTextField(
            value = viewModel.nameInput,
            modifier = Modifier.fillMaxWidth(),
            onValueChange = {
                viewModel.nameInput = it
            },
            label = { Text("Masukkan Nama Lengkap") },
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text = "Username",
            fontFamily = jakartaFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = TextLightPrimary
        )
        OutlinedTextField(
            value = viewModel.usernameInput,
            modifier = Modifier.fillMaxWidth(),
            onValueChange = {
                viewModel.usernameInput = it
            },
            label = { Text("Masukkan Username") }
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text = "Email",
            fontFamily = jakartaFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = TextLightPrimary
        )
        OutlinedTextField(
            value = viewModel.emailInput,
            modifier = Modifier.fillMaxWidth(),
            onValueChange = {
                viewModel.emailInput = it
            },
            label = { Text("Masukkan Alamat Email") }
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text = "Password",
            fontFamily = jakartaFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = TextLightPrimary
        )
        var isPasswordVisible by remember { mutableStateOf(false) }

        OutlinedTextField(
            value = viewModel.passwordInput,
            onValueChange = {
                viewModel.passwordInput = it
            },
            label = { Text("Masukkan Password") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,

            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),

            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),

            trailingIcon = {
                val image = if (isPasswordVisible)
                    Icons.Filled.Visibility
                else
                    Icons.Filled.VisibilityOff

                val description = if (isPasswordVisible) "Hide password" else "Show password"

                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(imageVector = image, contentDescription = description)
                }
            }
        )
        Spacer(
            modifier = Modifier.height(8.dp)
        )
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { viewModel.onRegisterClicked() },
        ) {

            if (viewModel.isLoading) {
                Text(
                    text = "Loading...",
                    fontSize = 18.sp,
                    fontFamily = jakartaFamily,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            } else {
                Text(
                    text = "Daftar",
                    fontSize = 18.sp,
                    fontFamily = jakartaFamily,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

        }
        Spacer(
            modifier = Modifier.height(8.dp)
        )
        OrSeparator()
        Spacer(
            modifier = Modifier.height(16.dp)
        )

        OutlinedButton(
            onClick = {
                launchGoogleSignIn()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, TextDarkSecondary),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.White
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
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

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        val annotatedText = buildAnnotatedString {
            append("Sudah punya akun? ")
            withStyle(
                style = SpanStyle(
                    color = Primary,
                    fontWeight = FontWeight.Bold
                )
            ) {
                append("Masuk di sini")
            }
        }

        Text(
            text = annotatedText,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    val intent = Intent(context, LoginActivity::class.java)
                    context.startActivity(intent)
                    (context as? Activity)?.finish()
                }

            )
                .fillMaxWidth()
        )

    }
}

@Composable
fun OrSeparator() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 1.dp,
            color = TextDarkSecondary
        )

        Text(
            text = "ATAU",
            color = TextDarkSecondary,
            fontFamily = jakartaFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 1.dp,
            color = TextDarkSecondary
        )



    }
}



@Preview(showBackground = true)
@Composable
fun GreetingPreview3() {
    TitipInTheme {
        RegisterScreen()
    }
}