package com.afsar.titipin.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afsar.titipin.data.remote.AuthRepository
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    var nameInput by mutableStateOf("")
    var usernameInput by mutableStateOf("")
    var emailInput by mutableStateOf("")
    var passwordInput by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var isRegisterSuccess by mutableStateOf(false)

    fun onGoogleSignInResult(task: Task<GoogleSignInAccount>) {
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken

            if (idToken != null) {
                isLoading = true
                errorMessage = null

                viewModelScope.launch {
                    repository.loginWithGoogle(idToken).collect { result ->
                        isLoading = false
                        result.onSuccess { isRegisterSuccess = true }
                        result.onFailure { errorMessage = it.localizedMessage ?: "Google Sign-In Gagal" }
                    }
                }
            } else {
                errorMessage = "Google Sign-In Gagal: Token tidak ditemukan"
            }
        } catch (e: ApiException) {
            errorMessage = "Google Sign-In Error: ${e.message}"
        }
    }

    fun onRegisterClicked() {
        if (nameInput.isBlank() || usernameInput.isBlank() || emailInput.isBlank() || passwordInput.isBlank()) {
            errorMessage = "Semua kolom wajib diisi!"
            return
        }

        isLoading = true
        errorMessage = null

        viewModelScope.launch {
            repository.register(nameInput, usernameInput, emailInput, passwordInput)
                .collect { result ->
                    isLoading = false
                    result.onSuccess {
                        isRegisterSuccess = true
                    }
                    result.onFailure { error ->
                        errorMessage = error.localizedMessage ?: "Terjadi kesalahan saat mendaftar"
                    }
                }
        }
    }
}