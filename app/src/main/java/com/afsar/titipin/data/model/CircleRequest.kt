package com.afsar.titipin.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CircleRequest(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderUsername: String = "",
    val receiverId: String = "",
    val status: String = "pending"
): Parcelable
