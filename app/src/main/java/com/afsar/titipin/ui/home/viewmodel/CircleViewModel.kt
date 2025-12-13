package com.afsar.titipin.ui.home.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afsar.titipin.data.model.Circle
import com.afsar.titipin.data.remote.repository.circle.CircleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CircleViewModel @Inject constructor(
    private val repository: CircleRepository
) : ViewModel() {

    // State dipisah agar UI bisa bereaksi terhadap Loading & Error
    var myCircles by mutableStateOf<List<Circle>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        loadMyCircles()
    }

    fun loadMyCircles() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            repository.getMyCircles().collect { result ->
                isLoading = false // Stop loading saat data diterima

                result.onSuccess { circles ->
                    myCircles = circles
                    errorMessage = null
                }
                result.onFailure { error ->
                    errorMessage = error.message ?: "Gagal memuat circle"
                }
            }
        }
    }
}