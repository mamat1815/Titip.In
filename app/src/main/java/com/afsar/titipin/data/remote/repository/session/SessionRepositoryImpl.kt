package com.afsar.titipin.data.remote.repository.session

import com.afsar.titipin.data.model.ChatMessage
import com.afsar.titipin.data.model.PaymentInfo
import com.afsar.titipin.data.model.Session
import com.afsar.titipin.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class SessionRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : SessionRepository {

    // Helper untuk path yang rapi
    private fun getSessionsCollection(circleId: String) =
        firestore.collection("circles").document(circleId).collection("sessions")

    private fun getCircleRef(circleId: String) =
        firestore.collection("circles").document(circleId)

    override fun createSession(session: Session): Flow<Result<Boolean>> = callbackFlow {
        val myUid = firebaseAuth.currentUser?.uid
        if (myUid == null) {
            trySend(Result.failure(Exception("User belum login")))
            close()
            return@callbackFlow
        }

        // 1. Ambil data User dulu untuk Creator Info
        try {
            val userSnapshot = firestore.collection("users").document(myUid).get().await()
            val myData = userSnapshot.toObject(User::class.java)

            if (myData != null) {
                // Gunakan Transaction agar Atomik:
                // (Buat Session BARU) + (Update status Circle jadi AKTIF)
                // Jika satu gagal, semua batal.
                firestore.runTransaction { transaction ->
                    val newSessionRef = getSessionsCollection(session.circleId).document()

                    val finalSession = session.copy(
                        id = newSessionRef.id,
                        creatorId = myUid,
                        creatorName = myData.name,
                        status = "open",
                        createdAt = null, // Biarkan null agar @ServerTimestamp bekerja
                        currentTitipCount = 0,
                        totalOmzet = 0.0
                    )

                    // Write 1: Simpan Session
                    transaction.set(newSessionRef, finalSession)

                    // Write 2: Update Parent Circle
                    // Memberitahu grup bahwa ada sesi belanja aktif sekarang
                    val circleRef = getCircleRef(session.circleId)
                    transaction.update(circleRef, mapOf(
                        "isActiveSession" to true,
                        "activeSessionId" to newSessionRef.id,
                        "lastMessage" to "${myData.name} membuka sesi titipan baru: ${session.title}",
                        "lastMessageTime" to com.google.firebase.Timestamp.now()
                    ))
                }.await()

                trySend(Result.success(true))
                close()
            } else {
                trySend(Result.failure(Exception("Data user tidak ditemukan")))
                close()
            }
        } catch (e: Exception) {
            trySend(Result.failure(e))
            close()
        }

        awaitClose { }
    }

    override fun getListSession(circleId: String): Flow<Result<List<Session>>> = callbackFlow {
        // Menampilkan sesi terbaru di atas
        val listener = getSessionsCollection(circleId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val sessions = snapshot.toObjects(Session::class.java)
                    trySend(Result.success(sessions))
                } else {
                    trySend(Result.success(emptyList()))
                }
            }

        awaitClose { listener.remove() }
    }

    override fun getSessionById(sessionId: String): Flow<Result<Session>> = callbackFlow {
        // Gunakan Collection Group "sessions"
        // Ini mencari ke seluruh database untuk dokumen di koleksi "sessions" yang punya field "id" == sessionId
        val listener = firestore.collectionGroup("sessions")
            .whereEqualTo("id", sessionId)
            .limit(1) // Kita cuma butuh 1
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    // Ambil dokumen pertama yang ditemukan
                    val session = snapshot.documents[0].toObject(Session::class.java)
                    if (session != null) {
                        trySend(Result.success(session))
                    } else {
                        trySend(Result.failure(Exception("Gagal parsing session")))
                    }
                } else {
                    trySend(Result.failure(Exception("Session tidak ditemukan")))
                }
            }

        awaitClose { listener.remove() }
    }

    override fun updateSession(
        circleId: String,
        sessionId: String,
        session: Session
    ): Flow<Result<Boolean>> = callbackFlow {
        // Gunakan SetOptions.merge() agar field lain (seperti createdAt) tidak hilang
        getSessionsCollection(circleId).document(sessionId)
            .set(session, SetOptions.merge())
            .addOnSuccessListener {
                trySend(Result.success(true))
                close()
            }
            .addOnFailureListener { e ->
                trySend(Result.failure(e))
                close()
            }
        awaitClose { }
    }

    override fun deleteSession(
        circleId: String,
        sessionId: String
    ): Flow<Result<Boolean>> = callbackFlow {
        // Hati-hati: Menghapus dokumen Session TIDAK menghapus sub-collection Orders di dalamnya.
        // Untuk skala besar, penghapusan sub-collection harus via Cloud Functions.
        // Tapi untuk MVP, kita hapus Session-nya saja agar tidak muncul di UI.

        firestore.runTransaction { transaction ->
            val sessionRef = getSessionsCollection(circleId).document(sessionId)
            val circleRef = getCircleRef(circleId)

            // 1. Hapus Session
            transaction.delete(sessionRef)

            // 2. Update Circle (Jika sesi yang dihapus adalah sesi aktif saat ini)
            // Kita set aktif jadi false
            // Note: Idealnya kita cek dulu apakah activeSessionId == sessionId,
            // tapi untuk simplifikasi, kita anggap jika dihapus, status grup jadi idle.
            transaction.update(circleRef, mapOf(
                "isActiveSession" to false,
                "activeSessionId" to null
            ))
        }.addOnSuccessListener {
            trySend(Result.success(true))
            close()
        }.addOnFailureListener { e ->
            trySend(Result.failure(e))
            close()
        }

        awaitClose { }
    }

    override fun getSessionChatMessages(sessionId: String): Flow<List<ChatMessage>> = callbackFlow {
        // Menggunakan Collection Group Query
        // Mencari di semua koleksi bernama "chats" yang punya field sessionId == param
        val listener = firestore.collectionGroup("chats")
            .whereEqualTo("sessionId", sessionId)
            .orderBy("timestamp", Query.Direction.ASCENDING) // Urutkan dari yang terlama ke terbaru
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val messages = snapshot?.toObjects(ChatMessage::class.java) ?: emptyList()
                trySend(messages)
            }

        awaitClose { listener.remove() }
    }

    override fun sendSessionChatMessage(
        circleId: String,
        sessionId: String,
        message: String
    ): Flow<Result<Boolean>> = callbackFlow {
        val myUid = firebaseAuth.currentUser?.uid
        if (myUid == null) {
            trySend(Result.failure(Exception("User belum login")))
            close()
            return@callbackFlow
        }

        // Ambil nama user dulu (opsional, bisa juga di-handle di backend/model user)
        firestore.collection("users").document(myUid).get()
            .addOnSuccessListener { userDoc ->
                val senderName = userDoc.getString("name") ?: "User"

                val chatRef = firestore.collection("circles")
                    .document(circleId)
                    .collection("sessions")
                    .document(sessionId)
                    .collection("chats")
                    .document()

                val newChat = ChatMessage(
                    id = chatRef.id,
                    sessionId = sessionId, // Penting untuk query collectionGroup
                    senderId = myUid,
                    senderName = senderName,
                    message = message,
                    timestamp = null // Biar server yang isi
                )

                chatRef.set(newChat)
                    .addOnSuccessListener {
                        trySend(Result.success(true))
                        close()
                    }
                    .addOnFailureListener {
                        trySend(Result.failure(it))
                        close()
                    }
            }
            .addOnFailureListener {
                trySend(Result.failure(it))
                close()
            }

        awaitClose { }
    }

    // Di dalam SessionRepositoryImpl
    override fun listenToPaymentsBySessionAndUser(
        sessionId: String,
        userId: String
    ): Flow<Result<List<PaymentInfo>>> = callbackFlow {

        // Query ke koleksi 'payments' (Root Collection)
        val listener = firestore.collection("payments")
            .whereEqualTo("sessionId", sessionId)
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }

                val payments = snapshot?.toObjects(PaymentInfo::class.java) ?: emptyList()
                trySend(Result.success(payments))
            }

        awaitClose { listener.remove() }
    }


}