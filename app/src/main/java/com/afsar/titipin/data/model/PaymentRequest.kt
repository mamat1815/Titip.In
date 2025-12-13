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

import com.google.gson.annotations.SerializedName

data class SnapTokenRequest(
    @SerializedName("session_id")
    val sessionId: String,

    @SerializedName("user_id")
    val userId: String,

    // Gunakan Long untuk Rupiah (Hindari Double untuk uang agar presisi)
    @SerializedName("amount")
    val amount: Long,

    @SerializedName("user_name")
    val userName: String,

    @SerializedName("user_email")
    val userEmail: String,

    @SerializedName("user_phone")
    val userPhone: String = "", // Tambahan: Penting untuk E-Wallet

    // Opsional: Kirim rincian barang agar muncul di halaman checkout Midtrans
    @SerializedName("item_name")
    val itemName: String = ""
)

data class SnapTokenResponse(
    @SerializedName("token") // Midtrans biasanya balikin key "token" bukan "snapToken"
    val snapToken: String,

    @SerializedName("redirect_url")
    val redirectUrl: String? = null,

    @SerializedName("order_id")
    val orderId: String
)

data class DisbursementRequest(
    @SerializedName("session_id")
    val sessionId: String,

    @SerializedName("jastiper_id")
    val jastiperId: String,

    // WAJIB: Kirim detail bank tujuan secara eksplisit demi keamanan
    @SerializedName("bank_code")
    val bankCode: String, // ex: "bca", "bri"

    @SerializedName("account_number")
    val accountNumber: String,

    @SerializedName("account_name") // Nama pemilik rek (untuk validasi)
    val accountName: String
)

data class DisbursementResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("total_collected")
    val totalCollected: Double = 0.0,

    @SerializedName("total_payment_fees")
    val totalPaymentFees: Double = 0.0,

    @SerializedName("disbursement_fee")
    val disbursementFee: Double = 0.0,

    @SerializedName("net_amount")
    val netAmount: Double = 0.0,

    @SerializedName("disbursement_id") // ID referensi internal kita
    val disbursementId: String = "",

    @SerializedName("midtrans_ref_id") // ID dari Midtrans Iris (Payouts)
    val midtransRefId: String = ""
)

data class PaymentStatusResponse(
    @SerializedName("transaction_status") // Midtrans field: capture, settlement, pending, deny, expire
    val status: String,

    @SerializedName("order_id")
    val orderId: String, // Penting biar tau order mana yang lunas

    @SerializedName("gross_amount")
    val amount: String, // Midtrans kadang balikin amount sebagai String "10000.00"

    @SerializedName("payment_type")
    val paymentType: String? = null,

    @SerializedName("transaction_time")
    val transactionTime: String? = null
)