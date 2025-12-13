package com.example.titipin.data.repository

import com.example.titipin.data.model.*

class ItemRepository {
    fun getScannedItems(): List<ReceiptItem> = listOf(
        ReceiptItem("Indomie Goreng", 2, "Budi", 6000),
        ReceiptItem("Susu Ultra Milk Cokelat 1L", 1, "Siti", 18000),
        ReceiptItem("Teh Botol Kotak", 3, "Tidak assign", 15000),
        ReceiptItem("Air Mineral", 2, "Budi", 6000)
    )
    
    fun getAssignableItems(): List<AssignableItem> = listOf(
        AssignableItem("Cheezy Freezy M", "25000", 1),
        AssignableItem("Red Bull M", "25000", 1),
        AssignableItem("Lemon Tea", "11000", 1)
    )
    
    fun getShoppingItems(): List<ShoppingItem> = listOf(
        ShoppingItem(1, "Roti Tawar", 2, "Jangan yang expired"),
        ShoppingItem(2, "Lemon Tea", 1, "Yang dingin ya"),
        ShoppingItem(3, "Cheezy Freezy M", 1, ""),
        ShoppingItem(4, "Red Bull M", 1, "Kalau ada yang sugar free")
    )
}
