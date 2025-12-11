package com.example.titipin.model

import com.example.titipin.R

// Enum untuk kategori titipan
enum class TitipanCategory(
    val categoryName: String,
    val iconRes: Int
) {
    MAKANAN_MINUMAN("Makanan/Minuman", R.drawable.ic_makanan),
    BELANJAAN("Belanjaan", R.drawable.ic_belanja),
    OBAT("Obat-obatan", R.drawable.ic_obat)
}
