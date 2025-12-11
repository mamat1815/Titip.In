package com.example.titipin.viewmodel

import androidx.lifecycle.ViewModel
import com.example.titipin.model.Transaction
import com.example.titipin.model.WeeklyStat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel : ViewModel() {

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _weeklyStats = MutableStateFlow<List<WeeklyStat>>(emptyList())
    val weeklyStats: StateFlow<List<WeeklyStat>> = _weeklyStats.asStateFlow()

    private val _userProfileUrl = MutableStateFlow("")
    val userProfileUrl: StateFlow<String> = _userProfileUrl.asStateFlow()


    init {
        loadDummyData()
    }



    private fun loadDummyData() {
        _transactions.value = listOf(
            Transaction(1, "Indomaret Jakal", "05 Oktober 2023", "Rp 60.000", "food"),
            Transaction(2, "28 Coffee", "04 Oktober 2023", "Rp 22.500", "coffee"),
            Transaction(3, "Apotek K24", "02 Oktober 2023", "Rp 163.000", "box"),
            Transaction(4, "Pizza Hut", "01 Oktober 2023", "Rp 25.000", "pizza", true),
        )

        _userProfileUrl.value = "https://cdn.antaranews.com/cache/1200x800/2018/11/jok4.jpg"

        _weeklyStats.value = listOf(
            WeeklyStat("S", 0.6f),
            WeeklyStat("S", 0.4f),
            WeeklyStat("R", 0.8f),
            WeeklyStat("K", 0.25f),
            WeeklyStat("J", 0.95f),
            WeeklyStat("S", 0.0f),
            WeeklyStat("M", 0.0f)
        )
    }
}