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
class HomeViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    // State untuk menampung data User
    // Karena User sudah Parcelable, object 'currentUser' ini nanti bisa dikirim via Intent
    var currentUser by mutableStateOf<User?>(null)
        private set // Setter private agar hanya bisa diubah di dalam ViewModel ini

    var isLoading by mutableStateOf(true)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    // Init block: Otomatis dipanggil saat ViewModel pertama kali dibuat
    init {
        fetchUserProfile()
    }

    // Fungsi untuk mengambil data profil dari Repository (Firestore)
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