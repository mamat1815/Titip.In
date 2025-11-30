package com.afsar.titipin.ui.home.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afsar.titipin.data.model.User
import com.afsar.titipin.data.remote.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    var currentUser by mutableStateOf<User?>(null)
        private set

    var isLoading by mutableStateOf(true)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        fetchUserProfile()
    }

    fun fetchUserProfile() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            repository.getUserProfile().collect { result ->
                isLoading = false
                result.onSuccess { user ->
                    currentUser = user
                }
                result.onFailure { error ->
                    errorMessage = error.localizedMessage ?: "Gagal memuat profil"
                }
            }
        }
    }

    // Fungsi Logout
    fun logout() {
        repository.logout()
        currentUser = null // Bersihkan data di memori
    }
}