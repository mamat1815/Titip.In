package com.afsar.titipin.data.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.parcelize.Parcelize
//
//@Parcelize
//data class DisbursementInfo(
//    val id: String = "",
//    val sessionId: String = "",
//    val jastiperId: String = "",
//    val jastiperName: String = "",
//
//    // Amounts
//    val totalCollected: Double = 0.0,
//    val totalPaymentFees: Double = 0.0,
//    val disbursementFee: Double = 0.0,
//    val netAmount: Double = 0.0,
//
//    // Bank details
//    val bankName: String = "",
//    val bankAccountNumber: String = "",
//    val bankAccountName: String = "",
//
//    // Status tracking
//    val status: String = "pending", // pending, processing, success, failed
//    val midtransReference: String = "",
//
//    // Timestamps
//    val requestedAt: Timestamp = Timestamp.now(),
//    val completedAt: Timestamp? = null,
//
//    // Payment breakdown
//    val paymentIds: List<String> = emptyList(),
//    val paymentCount: Int = 0
//) : Parcelable

@Parcelize
data class DisbursementInfo(

    val id: String = "",

    // Context (Penting untuk Query History/Admin Dashboard)
    val circleId: String = "",
    val sessionId: String = "",

    // Jastiper Info
    val jastiperId: String = "",
    val jastiperName: String = "",

    // Amounts (Pastikan presisi)
    val totalCollected: Double = 0.0,   // Total uang dari pemesan
    val totalPaymentFees: Double = 0.0, // Biaya gateway (masuk)
    val disbursementFee: Double = 0.0,  // Biaya transfer (keluar/Iris)
    val netAmount: Double = 0.0,        // Uang bersih yang diterima jastiper

    // Bank details (Sesuai format Midtrans Iris)
    val bankCode: String = "",          // PENTING: Midtrans butuh kode (ex: "bca", "bri"), bukan nama panjang
    val bankName: String = "",          // ex: "Bank Central Asia" (hanya untuk display UI)
    val bankAccountNumber: String = "",
    val bankAccountName: String = "",
    val beneficiaryEmail: String = "",  // Opsional: Untuk notifikasi email dari Midtrans

    // Status tracking
    val status: String = "pending", // pending, approved_by_midtrans, completed, failed, rejected

    // References & Idempotency
    val referenceNo: String = "",         // ID Unik yang KITA generate (biasanya: "DISB-{sessionId}-{timestamp}")
    val midtransId: String = "",          // ID dari MIDTRANS setelah request sukses

    // Error Handling (Penting!)
    val failureReason: String = "",       // Jika status == failed, simpan pesan error Midtrans di sini

    // Timestamps
    @ServerTimestamp
    val requestedAt: Timestamp? = null,   // Waktu kita request ke server
    val completedAt: Timestamp? = null,   // Waktu uang sukses masuk rekening (dari Webhook)

    // Payment breakdown (Audit Trail)
    val paymentIds: List<String> = emptyList(), // List ID Payment yang dicairkan dalam batch ini
    val paymentCount: Int = 0
) : Parcelable