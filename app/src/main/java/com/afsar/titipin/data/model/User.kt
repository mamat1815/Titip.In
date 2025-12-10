package com.afsar.titipin.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val uid: String = "",
    val name: String = "",
    val username: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val bankName: String = "",
    val bankAccountNumber: String = "",
    val bankAccountName: String = ""
) : Parcelable
