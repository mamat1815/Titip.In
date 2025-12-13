package com.afsar.titipin.data.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.parcelize.Parcelize

//@Parcelize
//data class PaymentInfo(
//    val id: String = "",
//    val sessionId: String = "",
//    val userId: String = "",  // Penitip user ID
//    val userName: String = "",
//    val amount: Double = 0.0,
//    val status: String = "pending", // pending, success, failed, expired
//    val snapToken: String = "",
//    val orderId: String = "",
//    val paymentType: String = "",
//    val transactionTime: Timestamp = Timestamp.now(),
//    val paidAt: Timestamp? = null
//) : Parcelable


@Parcelize
data class PaymentInfo(

    val id: String = "",
    val circleId: String = "", // Tambahan biar tau uang dari grup mana
    val sessionId: String = "",
    val userId: String = "",
    val amount: Double = 0.0,
    val status: String = "pending",
    val snapToken: String = "",
    val orderId: String = "", // Jika 1 payment untuk 1 order

    @ServerTimestamp
    val createdAt: Timestamp? = null,
    val paidAt: Timestamp? = null
) : Parcelable