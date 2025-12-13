package com.example.titipin.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.titipin.data.model.ParticipantRequest
import com.example.titipin.data.repository.SessionRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SessionDetailViewModel(
    private val repository: SessionRepository = SessionRepository()
) : ViewModel() {
    
    val participants: StateFlow<List<ParticipantRequest>> = repository.participants
    
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()
    
    private val _timeRemaining = MutableStateFlow(14 * 60 + 32) // 14:32 in seconds
    val timeRemaining: StateFlow<Int> = _timeRemaining.asStateFlow()
    
    init {
        startTimer()
    }
    
    fun acceptRequest(id: Int) {
        repository.acceptRequest(id)
    }
    
    fun rejectRequest(id: Int) {
        repository.rejectRequest(id)
    }
    
    fun selectTab(index: Int) {
        _selectedTab.value = index
    }
    
    private fun startTimer() {
        viewModelScope.launch {
            while (_timeRemaining.value > 0) {
                delay(1000)
                _timeRemaining.value -= 1
            }
        }
    }
}
