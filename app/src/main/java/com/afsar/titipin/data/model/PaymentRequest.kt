package com.afsar.titipin.data.model

data class SnapTokenRequest(
    val sessionId: String,
    val userId: String,
    val amount: Double,
    val userName: String,
    val userEmail: String
)

data class SnapTokenResponse(
    val snapToken: String,
    val orderId: String
)

data class PaymentStatusResponse(
    val status: String,
    val amount: Double,
    val paymentType: String? = null,
    val transactionTime: String? = null
)

data class DisbursementRequest(
    val sessionId: String,
    val jastiperId: String
)

data class DisbursementResponse(
    val success: Boolean,
    val message: String,
    val totalCollected: Double = 0.0,
    val totalPaymentFees: Double = 0.0,
    val disbursementFee: Double = 0.0,
    val netAmount: Double = 0.0,
    val disbursementId: String = ""
)
