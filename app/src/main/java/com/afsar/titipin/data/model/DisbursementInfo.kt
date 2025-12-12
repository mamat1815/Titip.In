package com.afsar.titipin.data.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class DisbursementInfo(
    val id: String = "",
    val sessionId: String = "",
    val jastiperId: String = "",
    val jastiperName: String = "",
    
    // Amounts
    val totalCollected: Double = 0.0,
    val totalPaymentFees: Double = 0.0,
    val disbursementFee: Double = 0.0,
    val netAmount: Double = 0.0,
    
    // Bank details
    val bankName: String = "",
    val bankAccountNumber: String = "",
    val bankAccountName: String = "",
    
    // Status tracking
    val status: String = "pending", // pending, processing, success, failed
    val midtransReference: String = "",
    
    // Timestamps
    val requestedAt: Timestamp = Timestamp.now(),
    val completedAt: Timestamp? = null,
    
    // Payment breakdown
    val paymentIds: List<String> = emptyList(),
    val paymentCount: Int = 0
) : Parcelable
