package com.afsar.titipin.data.model

import android.os.Parcelable
import com.google.firebase.firestore.DocumentId
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(

    val uid: String = "",
    val name: String = "",
    val username: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val fcmToken: String = "",
    val phoneNumber: String = "",
    val bank: Bank? = null
) : Parcelable