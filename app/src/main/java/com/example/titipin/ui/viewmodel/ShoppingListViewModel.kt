package com.example.titipin.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.titipin.data.model.ShoppingItem
import com.example.titipin.data.repository.ItemRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ShoppingListViewModel(
    private val repository: ItemRepository = ItemRepository()
) : ViewModel() {
    
    private val _shoppingItems = MutableStateFlow(repository.getShoppingItems())
    val shoppingItems: StateFlow<List<ShoppingItem>> = _shoppingItems.asStateFlow()
    
    fun toggleItemChecked(id: Int) {
        val updated = _shoppingItems.value.map {
            if (it.id == id) it.copy(isChecked = !it.isChecked) else it
        }
        _shoppingItems.value = updated
    }
}
