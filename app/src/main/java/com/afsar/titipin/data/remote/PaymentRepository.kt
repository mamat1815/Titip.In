//package com.afsar.titipin.data.remote
//
//import com.afsar.titipin.data.model.DisbursementResponse
//import com.afsar.titipin.data.model.PaymentInfo
//import com.afsar.titipin.data.model.SnapTokenResponse
//import kotlinx.coroutines.flow.Flow
//
//interface PaymentRepository {
//    suspend fun generateSnapToken(
//        sessionId: String,
//        userId: String,
//        amount: Double,
//        userName: String,
//        userEmail: String
//    ): Flow<Result<SnapTokenResponse>>
//
//    fun getPaymentStatus(orderId: String): Flow<Result<PaymentInfo?>>
//
//    fun getSessionPayments(sessionId: String): Flow<Result<List<PaymentInfo>>>
//
//    suspend fun disburseFunds(
//        sessionId: String,
//        jastiperId: String
//    ): Flow<Result<DisbursementResponse>>
//}

package com.afsar.titipin.data.remote

import com.afsar.titipin.data.model.DisbursementInfo
import com.afsar.titipin.data.model.DisbursementResponse
import com.afsar.titipin.data.model.PaymentInfo
import com.afsar.titipin.data.model.SnapTokenResponse
import kotlinx.coroutines.flow.Flow

interface PaymentRepository {
    suspend fun generateSnapToken(
        sessionId: String,
        userId: String,
        amount: Long, // GANTI Double ke Long (Best Practice Financial)
        userName: String,
        userEmail: String,
        userPhone: String // TAMBAHAN: Penting untuk E-Wallet
    ): Flow<Result<SnapTokenResponse>>

    // Mengambil status real-time dari database
    fun getPaymentStatus(orderId: String): Flow<Result<PaymentInfo?>>

    // History pembayaran dalam satu sesi
    fun getSessionPayments(sessionId: String): Flow<Result<List<PaymentInfo>>>

    suspend fun disburseFunds(
        sessionId: String,
        jastiperId: String,
        // TAMBAHAN: Kirim detail bank eksplisit
        bankCode: String,
        accountNumber: String,
        accountName: String
    ): Flow<Result<DisbursementResponse>>
    fun getDisbursementBySession(sessionId: String): Flow<Result<DisbursementInfo?>>

    fun getUserPaymentStatus(sessionId: String, userId: String): Flow<Result<String>> // return status: "success", "pending", "none"
}