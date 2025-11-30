package com.afsar.titipin.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Circle(
    val id: String = "",
    val name: String = "",
    val createdBy: String = "",
    val members: List<User> = emptyList(),
    val memberIds: List<String> = emptyList(),
    val isActiveSession: Boolean = false,
    val activeSessionId: String? = null
) : Parcelable
