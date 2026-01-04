package com.afsar.titipin.ui.home.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    // Fungsi sederhana untuk cek apakah user sedang login
    fun isUserLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }
}