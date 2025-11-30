package com.afsar.titipin.data.remote


import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override fun login(email: String, pass: String): Flow<Result<AuthResult>> = callbackFlow {
        // 1. Coba Sign In ke Firebase
        firebaseAuth.signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener { authResult ->
                trySend(Result.success(authResult)) // Kirim sinyal Sukses
            }
            .addOnFailureListener { e ->
                trySend(Result.failure(e)) // Kirim sinyal Gagal
            }
        awaitClose { /* Tidak ada resource yg perlu ditutup */ }
    }

    override fun register(name: String, username: String, email: String, pass: String): Flow<Result<AuthResult>> = callbackFlow {
        firebaseAuth.createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener { authResult ->
                val userId = authResult.user?.uid
                if (userId != null) {
                    val userMap = hashMapOf(
                        "uid" to userId,
                        "name" to name,
                        "username" to username,
                        "email" to email,
                        "createdAt" to com.google.firebase.Timestamp.now()
                    )
                    firestore.collection("users").document(userId).set(userMap)
                        .addOnSuccessListener { trySend(Result.success(authResult)) }
                        .addOnFailureListener { trySend(Result.failure(it)) }
                }
            }
            .addOnFailureListener { trySend(Result.failure(it)) }
        awaitClose { }
    }

    override fun loginWithGoogle(idToken: String): Flow<Result<AuthResult>> = callbackFlow {
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener { authResult ->
                val user = authResult.user
                val userId = user?.uid

                if (userId != null) {
                    // Cek dulu apakah data user sudah ada di Firestore?
                    val docRef = firestore.collection("users").document(userId)

                    docRef.get().addOnSuccessListener { document ->
                        if (document.exists()) {
                            // User lama login lagi -> Sukses langsung
                            trySend(Result.success(authResult))
                        } else {
                            // User baru (Login Google pertama kali) -> Simpan ke Firestore
                            val newUsername = user.email?.split("@")?.get(0) ?: "user${userId.take(5)}"

                            val userMap = hashMapOf(
                                "uid" to userId,
                                "name" to (user.displayName ?: "No Name"),
                                "username" to newUsername, // Username diambil dari depan email
                                "email" to (user.email ?: ""),
                                "createdAt" to com.google.firebase.Timestamp.now(),
                                "photoUrl" to (user.photoUrl?.toString() ?: "")
                            )

                            docRef.set(userMap)
                                .addOnSuccessListener { trySend(Result.success(authResult)) }
                                .addOnFailureListener { trySend(Result.failure(it)) }
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                trySend(Result.failure(e))
            }
        awaitClose { }
    }

    override fun logout() {
        firebaseAuth.signOut()
    }
}