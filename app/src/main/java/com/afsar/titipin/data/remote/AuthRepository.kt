package com.afsar.titipin.data.remote


import com.google.firebase.auth.AuthResult
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun login(email: String, pass: String): Flow<Result<AuthResult>>
    fun register(name: String, username: String, email: String, pass: String): Flow<Result<AuthResult>>

    // TAMBAHAN: Login pakai Google Token
    fun loginWithGoogle(idToken: String): Flow<Result<AuthResult>>

    fun logout()
}