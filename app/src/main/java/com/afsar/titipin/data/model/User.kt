package com.afsar.titipin.data.model

import android.os.Parcelable
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.parcelize.Parcelize
import java.util.Date
@IgnoreExtraProperties
@Parcelize
data class User(
    val uid: String = "",
    val name: String = "",
    val username: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val fcmToken: String = "",
    val phoneNumber: String = "",
    val feeSettings: Double = 0.0,
    val bank: Bank? = null,
    val stats: Stats? = null,
    val wallet: Wallet? = null,
    @ServerTimestamp
    val createdAt: Date? = null
) : Parcelable

@Parcelize
data class Stats(
    val totalTitip: Int = 0,
    val totalSesi: Int = 0,
    val totalCircle: Int = 0,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0
): Parcelable


@Parcelize
data class Wallet (
    val income: String = "",
    val expense: String = "",
): Parcelable



