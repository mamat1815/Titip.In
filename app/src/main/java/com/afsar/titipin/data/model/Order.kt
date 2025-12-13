package com.afsar.titipin.data.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class Order(

    val id: String = "",
    val circleId: String = "",
    val sessionId: String = "",

    val requesterId: String = "",
    val requesterName: String = "",
    val requesterPhotoUrl: String = "",

    val itemName: String = "",
    val itemImageUrl: String = "",
    val quantity: Int = 1,
    val notes: String = "",
    val priceEstimate: Double = 0.0,
    val jastipFee: Double = 0.0,
    val appFee: Double = 0.0,
    val totalPrice: Double = 0.0,

    // Status Flow: pending -> offered (jastiper kasih harga fix) -> paid -> bought -> received
    val status: String = "pending",
    @ServerTimestamp
    val timestamp: Timestamp? = null
) : Parcelable