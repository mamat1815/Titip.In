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


data class ChatMessages(
    val id: String,
    val senderName: String,
    val text: String,
    val timestamp: String,
    val isMe: Boolean, // True jika pesan dari kita (posisi kanan)
    val avatarRes: Int
)

data class CircleGroup(
    val id: String,
    val name: String,
    val membersNames: String,
    val lastSender : String,
    val lastMessage: String,
    val time: String, // Misal "12:30"
    val unreadCount: Int,
    val avatarRes: Int // Resource ID
)

data class ContactUser(
    val id: String,
    val name: String,
    val avatarRes: Int,
    var isSelected: Boolean = false // Untuk screen buat circle
)
// --- ENUM KHUSUS UI ---
enum class SessionType { HOST, GUEST }
enum class Category { FOOD, MEDICINE, SHOPPING }

// --- MODEL TAMPILAN HOME ---
data class TitipSession(
    val id: String,
    val merchantName: String,
    val description: String,
    val hostName: String = "",
    val type: SessionType,
    val category: Category,
    val status: String,
    val timer: String,
    val memberCount: Int,
    val avatars: List<Int>
)

data class TransactionHistory(
    val id: String,
    val merchantName: String,
    val date: String,
    val amount: String,
    val category: Category,
    val title: String = merchantName,
    val price: String = amount,
    val status: String = "Selesai"
)

data class UserProfile(val name: String, val avatarRes: Int)


// 1. SESSION
data class Sessions(
    val id: String,
    val locationName: String,
    val status: SessionStatus,
    val timeRemaining: String,
    val creatorName: String,
    val creatorAvatar: Int,
    val waitingCount: Int,
    val activeCount: Int,
    val totalItems: Int,
    val shoppingList: List<ShoppingItem> = emptyList()
)

// 2. SHOPPING ITEM
data class ShoppingItem(
    val id: String,
    val name: String,
    val requesterName: String,
    val price: String,
    val quantity: Int,
    var isChecked: Boolean = false
)

// 3. ENUMS
enum class SessionStatus(val stepIndex: Int) {
    WAITING_REQUEST(1),
    SHOPPING(2),
    DELIVERY(3),
    COMPLETED(4)
}

enum class RequestStatus {
    PENDING,
    ACCEPTED,
    REJECTED
}

// 4. REQUEST TRANSACTION
data class RequestTransaction(
    val id: String,
    val requesterName: String,
    val requesterAvatar: Int,
    val address: String, // <--- UPDATE: Tambah Alamat
    val items: List<ShoppingItem>,
    val note: String = "-",
    var status: RequestStatus = RequestStatus.PENDING
)

enum class PaymentStatus {
    PENDING, PAID
}

data class RequesterBill(
    val name: String,
    val avatarRes: Int,
    val address: String, // <--- UPDATE: Tambah Alamat di Tagihan
    val items: List<ShoppingItem>,
    val totalItemPrice: Long,
    val totalFee: Long,
    val grandTotal: Long,
    var status: PaymentStatus = PaymentStatus.PENDING,
    var isExpanded: Boolean = false
)



