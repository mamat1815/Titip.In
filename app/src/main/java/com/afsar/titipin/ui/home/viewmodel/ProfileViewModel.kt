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

    // Bank Account Form States
    var bankName by mutableStateOf("")
    var bankAccountNumber by mutableStateOf("")
    var bankAccountName by mutableStateOf("")
    var isSavingBankAccount by mutableStateOf(false)
    var bankAccountSaveSuccess by mutableStateOf<Boolean?>(null)

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
                    // Populate bank account fields if already registered
                    bankName = user.bankName
                    bankAccountNumber = user.bankAccountNumber
                    bankAccountName = user.bankAccountName
                }
                result.onFailure { error ->
                    errorMessage = error.localizedMessage ?: "Gagal memuat profil"
                }
            }
        }
    }

    fun updateBankAccount() {
        if (bankName.isBlank() || bankAccountNumber.isBlank() || bankAccountName.isBlank()) {
            errorMessage = "Semua field bank harus diisi"
            return
        }

        viewModelScope.launch {
            isSavingBankAccount = true
            bankAccountSaveSuccess = null

            repository.updateBankAccount(
                bankName = bankName,
                bankAccountNumber = bankAccountNumber,
                bankAccountName = bankAccountName
            ).collect { result ->
                isSavingBankAccount = false
                result.onSuccess {
                    bankAccountSaveSuccess = true
                    fetchUserProfile() // Refresh user data
                }
                result.onFailure { error ->
                    bankAccountSaveSuccess = false
                    errorMessage = error.localizedMessage ?: "Gagal menyimpan rekening bank"
                }
            }
        }
    }

    fun clearBankAccountMessage() {
        bankAccountSaveSuccess = null
        errorMessage = null
    }

    // Fungsi Logout
    fun logout() {
        repository.logout()
        currentUser = null // Bersihkan data di memori
    }
}