package com.afsar.titipin.ui.home.auth.login

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afsar.titipin.data.remote.AuthRepository
import com.afsar.titipin.ui.components.navigation.RootRoutes
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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



    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination = _startDestination.asStateFlow()

    init {
        checkActiveSession()
    }



    // Contoh Helper Function untuk dipanggil di MainActivity / ViewModel
    fun checkAndSaveFcmToken() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Gagal fetch token FCM", task.exception)
                return@addOnCompleteListener
            }

            // Token didapat
            val token = task.result
            Log.d("FCM", "Token saat ini: $token")

            // Update ke Firestore
            FirebaseFirestore.getInstance().collection("users")
                .document(uid)
                .update("fcmToken", token)
        }
    }

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

    fun checkActiveSession() {
        val currentUser = repository.getCurrentUserUid()
        if (currentUser != null) {
            _startDestination.value = RootRoutes.MAIN_APP
        } else {
            _startDestination.value = RootRoutes.LOGIN
        }
    }

    fun logout() {
        repository.logout()
        isLoginSuccess = false
        emailInput = ""
        passwordInput = ""

        _startDestination.value = RootRoutes.LOGIN
    }
}