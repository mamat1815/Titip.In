package com.afsar.titipin.data.remote

import com.afsar.titipin.data.model.DisbursementResponse
import com.afsar.titipin.data.model.PaymentInfo
import com.afsar.titipin.data.model.SnapTokenResponse
import kotlinx.coroutines.flow.Flow

interface PaymentRepository {
    suspend fun generateSnapToken(
        sessionId: String,
        userId: String,
        amount: Double,
        userName: String,
        userEmail: String
    ): Flow<Result<SnapTokenResponse>>

    fun getPaymentStatus(orderId: String): Flow<Result<PaymentInfo?>>

    fun getSessionPayments(sessionId: String): Flow<Result<List<PaymentInfo>>>

    suspend fun disburseFunds(
        sessionId: String,
        jastiperId: String
    ): Flow<Result<DisbursementResponse>>
}
