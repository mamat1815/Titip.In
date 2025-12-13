package com.example.titipin.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class InProgressViewModel : ViewModel() {
    
    private val _timeRemaining = MutableStateFlow("01:30:07")
    val timeRemaining: StateFlow<String> = _timeRemaining.asStateFlow()
    
    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()
    
    init {
        // Simulate pembeli finished scanning after 5 seconds
        viewModelScope.launch {
            delay(5000)
            _isScanning.value = true
        }
    }
}
