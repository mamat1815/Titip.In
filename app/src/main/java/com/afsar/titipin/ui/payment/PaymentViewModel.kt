package com.afsar.titipin.ui.payment

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afsar.titipin.data.remote.PaymentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class PaymentResult {
    object Success : PaymentResult()
    data class Failed(val message: String) : PaymentResult()
    object Pending : PaymentResult()
    object Canceled : PaymentResult()
}

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository
) : ViewModel() {

    var snapToken by mutableStateOf<String?>(null)
        private set

    var orderId by mutableStateOf<String?>(null)
        private set

    var isLoadingToken by mutableStateOf(false)
        private set

    var paymentResult by mutableStateOf<PaymentResult?>(null)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun initiatePayment(
        sessionId: String,
        userId: String,
        amount: Double,
        userName: String,
        userEmail: String
    ) {
        viewModelScope.launch {
            isLoadingToken = true
            errorMessage = null

            paymentRepository.generateSnapToken(
                sessionId = sessionId,
                userId = userId,
                amount = amount,
                userName = userName,
                userEmail = userEmail
            ).collect { result ->
                isLoadingToken = false
                result.onSuccess { response ->
                    snapToken = response.snapToken
                    orderId = response.orderId
                    Log.d("PaymentViewModel", "Snap token received: ${response.snapToken}")
                }
                result.onFailure { error ->
                    errorMessage = error.message ?: "Gagal memuat halaman pembayaran"
                    Log.e("PaymentViewModel", "Error getting snap token", error)
                }
            }
        }
    }

    fun handlePaymentResult(result: PaymentResult) {
        paymentResult = result
        Log.d("PaymentViewModel", "Payment result: $result")
    }


    fun clearError() {
        errorMessage = null
    }

    fun resetPayment() {
        snapToken = null
        orderId = null
        paymentResult = null
        errorMessage = null
    }
}
