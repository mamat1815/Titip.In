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
class LoginViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    var emailInput by mutableStateOf("")
    var passwordInput by mutableStateOf("")

    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var isLoginSuccess by mutableStateOf(false)

    fun onLoginClicked() {
        if (emailInput.isBlank() || passwordInput.isBlank()) {
            errorMessage = "Email dan Password tidak boleh kosong"
            return
        }

        isLoading = true
        errorMessage = null

        viewModelScope.launch {
            repository.login(emailInput, passwordInput)
                .collect { result ->
                    isLoading = false
                    result.onSuccess {
                        isLoginSuccess = true
                    }
                    result.onFailure { error ->
                        errorMessage = error.localizedMessage ?: "Login Gagal"
                    }
                }
        }
    }

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
                        result.onSuccess { isLoginSuccess = true }
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
}