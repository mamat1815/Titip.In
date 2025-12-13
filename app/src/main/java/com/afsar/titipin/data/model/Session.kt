package com.afsar.titipin.data.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class Session(

    val id: String = "",
    val circleId: String = "",
    val circleName: String = "",
    val creatorId: String = "",
    val creatorName: String = "",
    val title: String = "",
    val description: String = "",
    val locationName: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val durationMinutes: Int = 0,
    @ServerTimestamp
    val createdAt: Timestamp? = null,
    val closedAt: Timestamp? = null,
    val maxTitip: Int = 0,
    val currentTitipCount: Int = 0,
    val status: String = "open", // open, shopping, settling, completed, cancelled
// 2. Tambahkan @PropertyName agar cocok dengan database
    @get:PropertyName("revisionMode")
    @set:PropertyName("revisionMode")
    var isRevisionMode: Boolean = false,
    val totalOmzet: Double = 0.0
) : Parcelable
