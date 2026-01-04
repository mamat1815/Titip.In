package com.afsar.titipin.data.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.parcelize.Parcelize

//@Parcelize
//data class Order(
//
//    val id: String = "",
//    val circleId: String = "",
//    val sessionId: String = "",
//
//    val requesterId: String = "",
//    val requesterName: String = "",
//    val requesterPhotoUrl: String = "",
//
//    val itemName: String = "",
//    val itemImageUrl: String? = "",
//    val quantity: Int = 1,
//    val notes: String = "",
//    val priceEstimate: Double = 0.0,
//    val jastipFee: Double = 0.0,
//    val appFee: Double = 0.0,
//    val totalPrice: Double = 0.0,
//
//    val locationName: String = "",
//
//    val category: Category = Category.FOOD,
//
//    // Status Flow: pending -> offered (jastiper kasih harga fix) -> paid -> bought -> received
//    val status: String = "pending",
//    @ServerTimestamp
//    val timestamp: Timestamp? = null
//) : Parcelable
//package com.afsar.titipin.data.model
//
//import android.os.Parcelable
//import com.google.firebase.Timestamp
//import com.google.firebase.firestore.ServerTimestamp
//import kotlinx.parcelize.Parcelize

// 1. MODEL ITEM (Detail Barang)
@Parcelize
data class OrderItem(
    val name: String = "",
    val quantity: Int = 1,
    val priceEstimate: Double = 0.0, // Harga satuan
    val status: String = "pending",
    val notes: String = ""
) : Parcelable

// 2. MODEL ORDER UTAMA (Satu Transaksi User)
@Parcelize
data class Order(
    val id: String = "",
    val circleId: String = "",
    val sessionId: String = "",

    // Info User (Penitip)
    val requesterId: String = "",
    val requesterName: String = "",
    val requesterPhotoUrl: String = "",

    // --- LIST BARANG (Array di Firestore) ---
    val items: List<OrderItem> = emptyList(),

    // Info Pengantaran & Umum
    val deliveryLocation: String = "", // Lokasi pengantaran (misal: Kos Melati Kmr 3)
    val generalNotes: String = "",     // Catatan umum untuk jastiper

    // Keuangan (Total dari semua items)
    val totalEstimate: Double = 0.0,
    val jastipFee: Double = 0.0,
    val appFee: Double = 0.0,
    val finalTotalPrice: Double = 0.0, // Total Estimate + Fees

    // Status Transaksi
    val status: String = "pending", // pending, accepted, rejected, bought, done

    @ServerTimestamp
    val timestamp: Timestamp? = null
) : Parcelable