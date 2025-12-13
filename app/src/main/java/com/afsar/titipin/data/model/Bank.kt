package com.afsar.titipin.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Bank(
    val bankCode: String = "",
    val bankName: String = "",
    val bankAccountNumber: String = "",
    val bankAccountName: String = ""
) : Parcelable

