package com.afsar.titipin.data.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import kotlinx.parcelize.Parcelize

@Parcelize
data class Circle(

    val id: String = "",
    val name: String = "",
    val createdBy: String = "",
    val memberIds: List<String> = emptyList(),
    val activeSessionId: String? = null,
    val isActiveSession: Boolean = false,
    val lastMessage: String = "",
    val lastMessageTime: Timestamp? = null
) : Parcelable