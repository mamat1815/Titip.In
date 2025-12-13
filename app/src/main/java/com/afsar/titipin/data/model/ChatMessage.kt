package com.afsar.titipin.data.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class ChatMessage(
    @DocumentId
    val id: String = "",
    val sessionId: String = "", // WAJIB ADA untuk Query
    val senderId: String = "",
    val senderName: String = "",
    val message: String = "",

    @ServerTimestamp
    val timestamp: Timestamp? = null
): Parcelable