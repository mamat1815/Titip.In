package com.example.titipin.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.titipin.data.model.DititipiSession
import com.example.titipin.data.model.TitipSession
import com.example.titipin.data.model.TitipStatus
import com.example.titipin.data.repository.TitipRepository
import kotlinx.coroutines.flow.StateFlow

class TitipankuViewModel(
    private val repository: TitipRepository = TitipRepository()
) : ViewModel() {
    
    val titipSessions: StateFlow<List<TitipSession>> = repository.titipSessions
    val dititipiSessions: StateFlow<List<DititipiSession>> = repository.dititipiSessions
    
    fun toggleSessionStatus(id: Int) {
        val session = titipSessions.value.find { it.id == id }
        session?.let {
            when (it.status) {
                TitipStatus.MENUNGGU_ACCEPT -> repository.updateSessionStatus(id, TitipStatus.DIPROSES)
                else -> { /* No action */ }
            }
        }
    }
}
