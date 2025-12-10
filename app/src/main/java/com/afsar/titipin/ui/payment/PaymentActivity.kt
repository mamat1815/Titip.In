package com.afsar.titipin.ui.payment

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.afsar.titipin.BuildConfig
import com.afsar.titipin.ui.theme.TitipInTheme
import com.midtrans.sdk.uikit.external.UiKitApi
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PaymentActivity : ComponentActivity() {

    companion object {
        const val EXTRA_SESSION_ID = "session_id"
        const val EXTRA_USER_ID = "user_id"
        const val EXTRA_AMOUNT = "amount"
        const val EXTRA_USER_NAME = "user_name"
        const val EXTRA_USER_EMAIL = "user_email"
        
        const val RESULT_PAYMENT_SUCCESS = 1001
        const val RESULT_PAYMENT_FAILED = 1002
        const val RESULT_PAYMENT_PENDING = 1003
    }

    private val viewModel: PaymentViewModel by viewModels()
    private val merchantUrl = BuildConfig.MERCHANT_URL
    private val merchantClientKey = BuildConfig.MERCHANT_CLIENT_KEY

    private val paymentLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val resultCode = result.resultCode
        when (resultCode) {
            RESULT_OK -> {
                viewModel.handlePaymentResult(PaymentResult.Success)
                handlePaymentResult(PaymentResult.Success)
            }
            RESULT_CANCELED -> {
                viewModel.handlePaymentResult(PaymentResult.Canceled)
                handlePaymentResult(PaymentResult.Canceled)
            }
            else -> {
                viewModel.handlePaymentResult(PaymentResult.Failed("Payment failed"))
                handlePaymentResult(PaymentResult.Failed("Payment failed"))
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sessionId = intent.getStringExtra(EXTRA_SESSION_ID) ?: ""
        val userId = intent.getStringExtra(EXTRA_USER_ID) ?: ""
        val amount = intent.getDoubleExtra(EXTRA_AMOUNT, 0.0)
        val userName = intent.getStringExtra(EXTRA_USER_NAME) ?: ""
        val userEmail = intent.getStringExtra(EXTRA_USER_EMAIL) ?: ""

        UiKitApi.Builder()
            .withContext(this)
            .withMerchantUrl(merchantUrl)
            .withMerchantClientKey(merchantClientKey) // Sandbox client key
            .enableLog(true)
            .build()

        setContent {
            TitipInTheme {
                PaymentScreen(
                    viewModel = viewModel,
                    sessionId = sessionId,
                    userId = userId,
                    amount = amount,
                    userName = userName,
                    userEmail = userEmail,
                    onLaunchPayment = { snapToken ->
                        launchMidtransPayment(snapToken)
                    },
                    onPaymentComplete = { result ->
                        handlePaymentResult(result)
                    }
                )
            }
        }
    }
    
    private fun launchMidtransPayment(snapToken: String) {
        try {
            Log.d("PaymentActivity", "Launching Midtrans with token: $snapToken")
            UiKitApi.getDefaultInstance().startPaymentUiFlow(
                activity = this,
                launcher = paymentLauncher,
                snapToken = snapToken
            )
        } catch (e: Exception) {
            Log.e("PaymentActivity", "Error launching Midtrans", e)
            viewModel.handlePaymentResult(PaymentResult.Failed(e.message ?: "Launch failed"))
            handlePaymentResult(PaymentResult.Failed(e.message ?: "Launch failed"))
        }
    }

    private fun handlePaymentResult(result: PaymentResult) {
        val resultCode = when (result) {
            is PaymentResult.Success -> RESULT_PAYMENT_SUCCESS
            is PaymentResult.Failed -> RESULT_PAYMENT_FAILED
            is PaymentResult.Pending -> RESULT_PAYMENT_PENDING
            is PaymentResult.Canceled -> RESULT_CANCELED
        }
        
        setResult(resultCode)
        finish()
    }
}

@Composable
fun PaymentScreen(
    viewModel: PaymentViewModel,
    sessionId: String,
    userId: String,
    amount: Double,
    userName: String,
    userEmail: String,
    onLaunchPayment: (String) -> Unit,
    onPaymentComplete: (PaymentResult) -> Unit
) {
    val snapToken = viewModel.snapToken
    val isLoading = viewModel.isLoadingToken
    val errorMessage = viewModel.errorMessage

    LaunchedEffect(Unit) {
        viewModel.initiatePayment(
            sessionId = sessionId,
            userId = userId,
            amount = amount,
            userName = userName,
            userEmail = userEmail
        )
    }

    LaunchedEffect(snapToken) {
        if (snapToken != null) {
            onLaunchPayment(snapToken)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator()
                        Text("Memuat halaman pembayaran...")
                    }
                }
                errorMessage != null -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Text(
                            text = "Gagal memuat pembayaran",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.Red
                        )
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Button(onClick = { onPaymentComplete(PaymentResult.Failed(errorMessage)) }) {
                            Text("Tutup")
                        }
                    }
                }
                snapToken != null -> {
                    Text("Mengarahkan ke halaman pembayaran...")
                }
            }
        }
    }
}
