package com.example.titipin.model

data class Transaction(
    val id: Int,
    val title: String,
    val date: String,
    val amount: String,
    val iconType: String, // food, coffee, box, pizza
    val isCancelled: Boolean = false
)

data class WeeklyStat(
    val day: String,
    val percentage: Float // 0.0f sampai 1.0f
)


data class CircleGroup(
    val id: Int,
    val name: String,
    val memberCount: Int,
    val avatarUrl: String,
    var isSelected: Boolean = false
)