package com.example.titipin.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.titipin.data.model.ReceiptItem
import com.example.titipin.data.repository.ItemRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UploadReceiptViewModel(
    private val repository: ItemRepository = ItemRepository()
) : ViewModel() {
    
    private val _receiptItems = MutableStateFlow(repository.getScannedItems())
    val receiptItems: StateFlow<List<ReceiptItem>> = _receiptItems.asStateFlow()
    
    val assignedCount: StateFlow<Int> = MutableStateFlow(
        _receiptItems.value.count { it.assignedTo != "Tidak assign" }
    ).asStateFlow()
    
    val totalCount: StateFlow<Int> = MutableStateFlow(
        _receiptItems.value.size
    ).asStateFlow()
    
    val progress: StateFlow<Float> = MutableStateFlow(
        (assignedCount.value.toFloat() / totalCount.value.toFloat())
    ).asStateFlow()
}
