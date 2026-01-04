package com.afsar.titipin.data.model

//data class SnapTokenRequest(
//    val sessionId: String,
//    val userId: String,
//    val amount: Double,
//    val userName: String,
//    val userEmail: String
//)
//
//data class SnapTokenResponse(
//    val snapToken: String,
//    val orderId: String
//)
//
//data class PaymentStatusResponse(
//    val status: String,
//    val amount: Double,
//    val paymentType: String? = null,
//    val transactionTime: String? = null
//)
//
//data class DisbursementRequest(
//    val sessionId: String,
//    val jastiperId: String
//)
//
//data class DisbursementResponse(
//    val success: Boolean,
//    val message: String,
//    val totalCollected: Double = 0.0,
//    val totalPaymentFees: Double = 0.0,
//    val disbursementFee: Double = 0.0,
//    val netAmount: Double = 0.0,
//    val disbursementId: String = ""
//)
//package com.afsar.titipin.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp
import com.google.gson.annotations.SerializedName

// --- REQUEST GENERATE TOKEN ---
data class SnapTokenRequest(
    @SerializedName("sessionId") val sessionId: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("amount") val amount: Long, // Subtotal (Barang + Tip)
    @SerializedName("userName") val userName: String,
    @SerializedName("userEmail") val userEmail: String,
    @SerializedName("userPhone") val userPhone: String
)

// --- RESPONSE GENERATE TOKEN ---
data class SnapTokenResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("snap_token") val snapToken: String,
    @SerializedName("order_id") val orderId: String,
    @SerializedName("redirect_url") val redirectUrl: String?
)

// --- REQUEST DISBURSEMENT ---
data class DisbursementRequest(
    @SerializedName("sessionId") val sessionId: String,
    @SerializedName("jastiperId") val jastiperId: String,
    @SerializedName("bankCode") val bankCode: String, // Contoh: "bca", "bri"
    @SerializedName("accountNumber") val accountNumber: String,
    @SerializedName("accountName") val accountName: String
)

// --- RESPONSE DISBURSEMENT ---
data class DisbursementResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("total_collected") val totalCollected: Double,
    @SerializedName("net_amount") val netAmount: Double,
    @SerializedName("disbursement_id") val disbursementId: String?
)

// --- FIRESTORE PAYMENT INFO (Untuk Read Data) ---
// Pastikan class ini punya empty constructor untuk Firestore .toObject()
data class PaymentInfo(
    val id: String = "",
    val orderId: String = "",
    val sessionId: String = "",
    val userId: String = "",
    val amount: Double = 0.0, // Subtotal
    val adminFee: Double = 0.0, // Fee App
    val grossAmount: Double = 0.0, // Total yg dibayar user
    val status: String = "pending", // pending, success, failed
    val paymentType: String = "",
    // ... field timestamp dll
    @ServerTimestamp
    val createdAt: Timestamp? = null,
    val paidAt: Timestamp? = null
)
