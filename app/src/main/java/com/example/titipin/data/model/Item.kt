package com.example.titipin.data.model

// Receipt/Upload Models
data class ReceiptItem(
    val name: String,
    val quantity: Int,
    val assignedTo: String,
    val price: Int
)

// Assignment Models
data class AssignableItem(
    val name: String,
    val price: String,
    val totalUnits: Int,
    var assignedCount: Int = 0,
    var isAssigned: Boolean = false
)

// Shopping List Models
data class ShoppingItem(
    val id: Int,
    val name: String,
    val quantity: Int,
    val notes: String = "",
    var isChecked: Boolean = false
)
