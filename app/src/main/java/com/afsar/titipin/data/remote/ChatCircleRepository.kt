package com.afsar.titipin.data.remote

import android.util.Log
import com.afsar.titipin.data.model.ChatMessage
import com.afsar.titipin.data.model.ChatMessages
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ChatCircleRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {

    val currentUserId: String? get() = auth.currentUser?.uid

    // 1. Ambil Pesan Realtime
    fun getMessages(circleId: String): Flow<List<ChatMessages>> = callbackFlow {
        val collectionRef = firestore.collection("circles")
            .document(circleId)
            .collection("chats")
            .orderBy("createdAt", Query.Direction.ASCENDING) // Urutkan dari yang terlama ke terbaru

        val listener = collectionRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val messages = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(ChatMessages::class.java)?.copy(id = doc.id)
            } ?: emptyList()

            trySend(messages)
        }

        awaitClose { listener.remove() }
    }

    // 2. Kirim Pesan
    suspend fun sendMessage(circleId: String, messageText: String): Result<Boolean> {
        val uid = currentUserId ?: return Result.failure(Exception("User not logged in"))
        val user = auth.currentUser

        // Data pesan
        val newMessage = hashMapOf(
            "senderId" to uid,
            "senderName" to (user?.displayName ?: "User"),
            "avatarUrl" to (user?.photoUrl?.toString() ?: ""),
            "message" to messageText,
            "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
        )

        return try {
            firestore.collection("circles")
                .document(circleId)
                .collection("chats")
                .add(newMessage)
                .await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ...
    // 3. Ambil Info Circle (Nama, Avatar, & Member Count)
    suspend fun getCircleInfo(circleId: String): Map<String, Any> {
        return try {
            val snapshot = firestore.collection("circles").document(circleId).get().await()
            val members = snapshot.get("members") as? List<*> // Ambil array members

            mapOf(
                "name" to (snapshot.getString("name") ?: "Circle"),
                "avatarUrl" to (snapshot.getString("avatarUrl") ?: ""),
                "memberCount" to (members?.size ?: 0) // Hitung jumlahnya
            )
        } catch (e: Exception) {
            mapOf("name" to "Circle", "avatarUrl" to "", "memberCount" to 0)
        }
    }
// ...
}