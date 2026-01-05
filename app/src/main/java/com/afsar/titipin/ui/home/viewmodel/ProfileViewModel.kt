package com.afsar.titipin.ui.home.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afsar.titipin.data.model.User
import com.afsar.titipin.data.remote.AuthRepository
import com.afsar.titipin.data.remote.profile.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    var currentUser by mutableStateOf<User?>(null)
    var isLoading by mutableStateOf(false)

    init {
        fetchUserProfile()
    }

    fun fetchUserProfile() {
        isLoading = true
        viewModelScope.launch {
            val uid = authRepository.getCurrentUser()?.uid
            if (uid != null) {
                // 1. Ambil User Profile
                authRepository.getUserProfile().collect { res ->
                    res.onSuccess { user ->
                        // Simpan sementara user basic
                        currentUser = user

                        // 2. Hitung Statistik (Income, Expense, Sesi)
                        val stats = profileRepository.fetchUserStats()

                        // 3. Update user dengan data stats terbaru
                        currentUser = user.copy(stats = stats)
                    }
                    isLoading = false
                }
            } else {
                isLoading = false
            }
        }
    }

    fun logout() {
        authRepository.logout()
    }

    // Helper: Format Double ke String Rupiah (Rp 50.000)
    fun formatRupiah(amount: Double): String {
        val formatRp = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return formatRp.format(amount).replace("Rp", "Rp ").substringBeforeLast(",00")
    }
}