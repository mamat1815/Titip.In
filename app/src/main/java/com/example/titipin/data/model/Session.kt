package com.example.titipin.data.model

import androidx.compose.ui.graphics.Color

// Session Detail Models
data class ParticipantRequest(
    val id: Int,
    val name: String,
    val circleName: String,
    val orderItems: List<OrderItem>,
    val notes: String,
    val amount: String,
    val status: RequestStatus,
    val avatarRes: Int
)

data class OrderItem(
    val name: String,
    val quantity: Int
)

enum class RequestStatus {
    PENDING, ACCEPTED, REJECTED
}

// Titipanku Models
data class TitipSession(
    val id: Int,
    val title: String,
    val recipientName: String,
    val location: String,
    val category: String,
    val iconRes: Int,
    val status: TitipStatus,
    val amount: String,
    val requestItem: String,
    val requestQty: Int
)

enum class TitipStatus(val displayName: String, val color: Color) {
    MENUNGGU_ACCEPT("Menunggu Accept", Color(0xFFF59E0B)), // Orange
    DITOLAK("Ditolak", Color(0xFFE53935)), // Red
    DIPROSES("Diproses", Color(0xFF3B82F6)), // Blue
    BAYAR_DAN_ANTAR("Bayar & Antar", Color(0xFF10B981)) // Green
}

data class DititipiSession(
    val id: Int,
    val title: String,
    val location: String,
    val category: String,
    val iconRes: Int,
    val participantCount: Int,
    val status: DititipiStatus,
    val timeRemaining: String
)

enum class DititipiStatus(val displayName: String, val color: Color) {
    MENUNGGU_PESANAN("Menunggu Pesanan", Color(0xFFF59E0B)),
    BELANJA("Sedang Belanja", Color(0xFF3B82F6)),
    ANTAR("Sedang Diantar", Color(0xFF10B981)),
    SELESAI("Selesai", Color(0xFF6B7280))
}
