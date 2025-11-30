package com.afsar.titipin.data.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class JastipSession(
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
    val maxTitip: Int = 0,
    val status: String = "open",
    val totalOrders: Int = 0,
    val participantIds: List<String> = emptyList(),
    val createdAt: Timestamp = Timestamp.now()
) : Parcelable
