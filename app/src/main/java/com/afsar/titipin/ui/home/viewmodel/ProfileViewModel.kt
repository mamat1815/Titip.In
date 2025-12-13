package com.afsar.titipin.ui.home.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afsar.titipin.data.model.Bank
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

    // State untuk Form Bank
    var bankCode by mutableStateOf("") // ex: "bca", "mandiri"
    var bankName by mutableStateOf("") // ex: "Bank Central Asia"
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

                    // --- PERBAIKAN: Safe Call (?.) agar tidak crash jika bank null ---
                    bankCode = user.bank?.bankCode ?: ""
                    bankName = user.bank?.bankName ?: ""
                    bankAccountNumber = user.bank?.bankAccountNumber ?: ""
                    bankAccountName = user.bank?.bankAccountName ?: ""
                }
                result.onFailure { error ->
                    errorMessage = error.localizedMessage ?: "Gagal memuat profil"
                }
            }
        }
    }

    fun updateBankAccount() {
        // Validasi input
        if (bankCode.isBlank() || bankName.isBlank() || bankAccountNumber.isBlank() || bankAccountName.isBlank()) {
            errorMessage = "Semua data bank wajib diisi"
            return
        }

        viewModelScope.launch {
            isSavingBankAccount = true
            bankAccountSaveSuccess = null
            errorMessage = null // Reset error

            // Buat object Bank
            val bankData = Bank(
                bankCode = bankCode,
                bankName = bankName,
                bankAccountNumber = bankAccountNumber,
                bankAccountName = bankAccountName
            )

            // Pastikan AuthRepository punya fungsi updateBankAccount yang menerima object Bank
            repository.updateBankAccount(bankData).collect { result ->
                isSavingBankAccount = false
                result.onSuccess {
                    bankAccountSaveSuccess = true
                    fetchUserProfile() // Refresh data user di UI
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
        // Opsional: Reset form ke data user asli jika batal edit
        currentUser?.bank?.let {
            bankCode = it.bankCode
            bankName = it.bankName
            bankAccountNumber = it.bankAccountNumber
            bankAccountName = it.bankAccountName
        }
    }

    fun logout() {
        repository.logout()
        currentUser = null
    }
}