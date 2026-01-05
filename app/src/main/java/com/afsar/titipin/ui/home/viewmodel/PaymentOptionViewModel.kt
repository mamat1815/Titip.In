package com.afsar.titipin.ui.home.viewmodel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afsar.titipin.data.model.Bank
import com.afsar.titipin.data.remote.AuthRepository
import com.afsar.titipin.data.remote.profile.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaymentOptionViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    var bankName by mutableStateOf("")
    var accountNumber by mutableStateOf("")
    var accountName by mutableStateOf("")

    var isLoading by mutableStateOf(false)
    var uiMessage by mutableStateOf<String?>(null)

    init {
        viewModelScope.launch {
//            val user = authRepository.getUserProfile(authRepository.getCurrentUser()?.uid ?: "").first().getOrNull()
             authRepository.getUserProfile().collect {
                it.onSuccess { user ->
                    bankName = user.bank?.bankCode ?: ""
                    accountNumber = user.bank?.bankAccountNumber ?: ""
                    accountName = user.bank?.bankAccountName ?: ""
                }
                it.onFailure { error ->
                    uiMessage = error.localizedMessage ?: "Gagal memuat profil"
                }
            }
//            user?.bank?.let {
//                bankName = it.bankCode
//                accountNumber = it.bankAccountNumber
//                accountName = it.bankAccountName
//            }
        }
    }

    fun saveBank() {
        if (bankName.isBlank() || accountNumber.isBlank() || accountName.isBlank()) {
            uiMessage = "Mohon lengkapi semua data"
            return
        }
        isLoading = true
        val bank = Bank(
            bankName = bankName,
            bankAccountNumber = accountNumber,
            bankAccountName = accountName,
            bankCode = "bca"
        )
        viewModelScope.launch {
            profileRepository.updateBankAccount(bank).collect {
                isLoading = false
                uiMessage = if (it.isSuccess) "Rekening berhasil disimpan!" else "Gagal menyimpan: ${it.exceptionOrNull()?.message}"
            }
        }
    }

    fun clearMessage() { uiMessage = null }
}