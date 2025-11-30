package com.afsar.titipin.data.local

import androidx.compose.ui.graphics.vector.ImageVector

data class TransactionHistory(
    val title: String,
    val date: String,
    val price: String,
    val icon: ImageVector // Menggunakan icon vector sementara
)

// Data Dummy untuk Chart Pendapatan
data class DailyIncome(
    val day: String,
    val percentage: Float // 0.0f - 1.0f untuk tinggi bar
)