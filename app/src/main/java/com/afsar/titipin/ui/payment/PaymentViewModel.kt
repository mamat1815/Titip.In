//package com.afsar.titipin.ui.payment
//
//import android.util.Log
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.setValue
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.afsar.titipin.data.remote.PaymentRepository
//import dagger.hilt.android.lifecycle.HiltViewModel
//import kotlinx.coroutines.launch
//import javax.inject.Inject
//
//sealed class PaymentResult {
//    object Success : PaymentResult()
//    data class Failed(val message: String) : PaymentResult()
//    object Pending : PaymentResult()
//    object Canceled : PaymentResult()
//}
//
//@HiltViewModel
//class PaymentViewModel @Inject constructor(
//    private val paymentRepository: PaymentRepository
//) : ViewModel() {
//
//    var snapToken by mutableStateOf<String?>(null)
//        private set
//
//    var orderId by mutableStateOf<String?>(null)
//        private set
//
//    var isLoadingToken by mutableStateOf(false)
//        private set
//
//    var paymentResult by mutableStateOf<PaymentResult?>(null)
//        private set
//
//    var errorMessage by mutableStateOf<String?>(null)
//        private set
//
//    fun initiatePayment(
//        sessionId: String,
//        userId: String,
//        amount: Double,
//        userName: String,
//        userEmail: String
//    ) {
//        viewModelScope.launch {
//            isLoadingToken = true
//            errorMessage = null
//
//            paymentRepository.generateSnapToken(
//                sessionId = sessionId,
//                userId = userId,
//                amount = amount,
//                userName = userName,
//                userEmail = userEmail
//            ).collect { result ->
//                isLoadingToken = false
//                result.onSuccess { response ->
//                    snapToken = response.snapToken
//                    orderId = response.orderId
//                    Log.d("PaymentViewModel", "Snap token received: ${response.snapToken}")
//                }
//                result.onFailure { error ->
//                    errorMessage = error.message ?: "Gagal memuat halaman pembayaran"
//                    Log.e("PaymentViewModel", "Error getting snap token", error)
//                }
//            }
//        }
//    }
//
//    fun handlePaymentResult(result: PaymentResult) {
//        paymentResult = result
//        Log.d("PaymentViewModel", "Payment result: $result")
//    }
//
//
//    fun clearError() {
//        errorMessage = null
//    }
//
//    fun resetPayment() {
//        snapToken = null
//        orderId = null
//        paymentResult = null
//        errorMessage = null
//    }
//}


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

// Sealed Class untuk menghandle status callback dari Midtrans SDK
sealed class PaymentResult {
    object Idle : PaymentResult()
    object Success : PaymentResult()
    object Pending : PaymentResult()
    object Canceled : PaymentResult()
    data class Failed(val message: String) : PaymentResult()
}

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository
) : ViewModel() {

    // --- State Variables ---
    var snapToken by mutableStateOf<String?>(null)
        private set

    var orderId by mutableStateOf<String?>(null)
        private set

    var isLoadingToken by mutableStateOf(false)
        private set

    var paymentResult by mutableStateOf<PaymentResult>(PaymentResult.Idle)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    // --- Functions ---

    fun initiatePayment(
        sessionId: String,
        userId: String,
        amount: Long,
        userName: String,
        userEmail: String,
        userPhone: String
    ) {
        viewModelScope.launch {
            isLoadingToken = true
            errorMessage = null
            snapToken = null

            // Konversi Double ke Long (Rupiah penuh)
//            val amountLong = amount.toLong()

            paymentRepository.generateSnapToken(
                sessionId = sessionId,
                userId = userId,
                amount = amount,
                userName = userName,
                userEmail = userEmail,
                userPhone = userPhone
            ).collect { result ->
                isLoadingToken = false

                // --- PERBAIKAN: Gunakan .fold() ---
                result.fold(
                    onSuccess = { response ->
                        orderId = response.orderId
                        snapToken = response.snapToken
                        Log.d("PaymentViewModel", "Snap token received: ${response.snapToken}")
                    },
                    onFailure = { error ->
                        errorMessage = error.message ?: "Gagal membuat transaksi"
                        Log.e("PaymentViewModel", "Error getting snap token", error)
                    }
                )
            }
        }
    }

    fun handlePaymentResult(result: PaymentResult) {
        paymentResult = result
        Log.d("PaymentViewModel", "UI Payment Result Update: $result")
    }

    fun clearError() {
        errorMessage = null
    }

    fun resetPaymentState() {
        snapToken = null
        orderId = null
        paymentResult = PaymentResult.Idle
        errorMessage = null
        isLoadingToken = false
    }
}