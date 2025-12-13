package com.example.titipin.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.titipin.data.model.AssignableItem
import com.example.titipin.data.repository.ItemRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PenitipLiveAssignmentViewModel(
    private val repository: ItemRepository = ItemRepository()
) : ViewModel() {
    
    private val _items = MutableStateFlow(repository.getAssignableItems())
    val items: StateFlow<List<AssignableItem>> = _items.asStateFlow()
    
    private val _allAssigned = MutableStateFlow(false)
    val allAssigned: StateFlow<Boolean> = _allAssigned.asStateFlow()
    
    fun assignItem(itemIndex: Int, assignments: Map<String, Int>) {
        val updatedItems = _items.value.toMutableList()
        val item = updatedItems[itemIndex]
        
        item.assignedCount = assignments.values.sum()
        item.isAssigned = item.assignedCount == item.totalUnits
        
        updatedItems[itemIndex] = item
        _items.value = updatedItems
        
        // Check if all items are assigned
        _allAssigned.value = updatedItems.all { it.isAssigned }
    }
}
