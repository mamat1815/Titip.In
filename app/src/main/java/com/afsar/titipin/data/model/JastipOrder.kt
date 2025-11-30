package com.afsar.titipin.data.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class JastipOrder(
    val id: String = "",
    val sessionId: String = "",
    val requesterId: String = "",
    val requesterName: String = "",
    val requesterPhotoUrl: String = "",
    val itemName: String = "",
    val quantity: Int = 1,
    val notes: String = "",
    val priceEstimate: Double = 0.0,
    val status: String = "pending", // pending, accepted, rejected, bought, out_of_stock
    val timestamp: Timestamp = Timestamp.now()
) : Parcelable