package com.afsar.titipin.data.remote


import android.util.Log
import com.afsar.titipin.data.model.ChatMessage
import com.afsar.titipin.data.model.Circle
import com.afsar.titipin.data.model.CircleRequest
import com.afsar.titipin.data.model.JastipOrder
import com.afsar.titipin.data.model.JastipSession
import com.afsar.titipin.data.model.PaymentInfo
import com.afsar.titipin.data.model.User
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override fun login(email: String, pass: String): Flow<Result<AuthResult>> = callbackFlow {
        firebaseAuth.signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener { authResult ->
                trySend(Result.success(authResult))
            }
            .addOnFailureListener { e ->
                trySend(Result.failure(e))
            }
        awaitClose {}
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
                    val docRef = firestore.collection("users").document(userId)

                    docRef.get().addOnSuccessListener { document ->
                        if (document.exists()) {
                            trySend(Result.success(authResult))
                        } else {
                            val newUsername = user.email?.split("@")?.get(0) ?: "user${userId.take(5)}"

                            val userMap = hashMapOf(
                                "uid" to userId,
                                "name" to (user.displayName ?: "No Name"),
                                "username" to newUsername,
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

    override fun getUserProfile(): Flow<Result<User>> = callbackFlow {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            val uid = currentUser.uid

            firestore.collection("users").document(uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val user = document.toObject(User::class.java)
                        if (user != null) {
                            trySend(Result.success(user))
                        } else {
                            trySend(Result.failure(Exception("Gagal parsing data user")))
                        }
                    } else {
                        trySend(Result.failure(Exception("Data user tidak ditemukan")))
                    }
                }
                .addOnFailureListener { e ->
                    trySend(Result.failure(e))
                }
        } else {
            trySend(Result.failure(Exception("User belum login")))
        }
        awaitClose { }
    }

    override fun searchUsers(query: String): Flow<Result<List<User>>> = callbackFlow {
        if (query.isBlank()) {
            trySend(Result.success(emptyList()))
            awaitClose { }
            return@callbackFlow
        }

        firestore.collection("users")
            .whereGreaterThanOrEqualTo("username", query)
            .whereLessThan("username", query + "\uf8ff")
            .get()
            .addOnSuccessListener { snapshot ->
                val users = snapshot.toObjects(User::class.java)
                val myUid = firebaseAuth.currentUser?.uid
                val filteredUsers = users.filter { it.uid != myUid }
                trySend(Result.success(filteredUsers))
            }
            .addOnFailureListener { trySend(Result.failure(it)) }
        awaitClose { }
    }

    override fun sendCircleRequest(receiverId: String): Flow<Result<Boolean>> = callbackFlow {
        val myUid = firebaseAuth.currentUser?.uid
        if (myUid == null) {
            trySend(Result.failure(Exception("Not login")))
            awaitClose { }
            return@callbackFlow
        }
        firestore.collection("users").document(myUid).get().addOnSuccessListener { doc ->
            val myData = doc.toObject(User::class.java)
            if (myData != null) {
                val requestId = firestore.collection("circle_requests").document().id
                val request = CircleRequest(
                    id = requestId,
                    senderId = myUid,
                    senderName = myData.name,
                    senderUsername = myData.username,
                    receiverId = receiverId,
                    status = "pending"
                )

                firestore.collection("circle_requests").document(requestId).set(request)
                    .addOnSuccessListener { trySend(Result.success(true)) }
                    .addOnFailureListener { trySend(Result.failure(it)) }
            }
        }
        awaitClose { }
    }

    override fun getIncomingRequests(): Flow<Result<List<CircleRequest>>> = callbackFlow {
        val myUid = firebaseAuth.currentUser?.uid ?: ""

        firestore.collection("circle_requests")
            .whereEqualTo("receiverId", myUid)
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                val requests = snapshot?.toObjects(CircleRequest::class.java) ?: emptyList()
                trySend(Result.success(requests))
            }
        awaitClose { }
    }

    override fun respondToRequest(requestId: String, isAccepted: Boolean): Flow<Result<Boolean>> = callbackFlow {
        val newStatus = if (isAccepted) "accepted" else "rejected"
        firestore.collection("circle_requests").document(requestId)
            .update("status", newStatus)
            .addOnSuccessListener { trySend(Result.success(true)) }
            .addOnFailureListener { trySend(Result.failure(it)) }
        awaitClose { }
    }

    override fun createCircle(name: String, members: List<User>): Flow<Result<Boolean>> = callbackFlow {
        val myUid = firebaseAuth.currentUser?.uid
        if (myUid == null) {
            trySend(Result.failure(Exception("User belum login")))
            awaitClose { }
            return@callbackFlow
        }

        firestore.collection("users").document(myUid).get().addOnSuccessListener { userDoc ->
            val myUser = userDoc.toObject(User::class.java)

            if (myUser != null) {
                val finalMembers = members.toMutableList()
                if (finalMembers.none { it.uid == myUid }) {
                    finalMembers.add(myUser)
                }

                val memberIds = finalMembers.map { it.uid }

                val newCircleRef = firestore.collection("circles").document()
                val newCircleId = newCircleRef.id

                val newCircle = Circle(
                    id = newCircleId,
                    name = name,
                    createdBy = myUid,
                    members = finalMembers,
                    memberIds = memberIds,
                    isActiveSession = false
                )
                newCircleRef.set(newCircle)
                    .addOnSuccessListener {
                        trySend(Result.success(true))
                    }
                    .addOnFailureListener { e ->
                        trySend(Result.failure(e))
                    }
            } else {
                trySend(Result.failure(Exception("Gagal mengambil data profil sendiri")))
            }
        }.addOnFailureListener {
            trySend(Result.failure(it))
        }

        awaitClose { }
    }

    override fun getMyCircles(): Flow<Result<List<Circle>>> = callbackFlow {
        val myUid = firebaseAuth.currentUser?.uid
        if (myUid == null) {
            trySend(Result.failure(Exception("User belum login")))
            awaitClose { }
            return@callbackFlow
        }

        firestore.collection("circles")
            .whereArrayContains("memberIds", myUid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val circles = snapshot.toObjects(Circle::class.java)
                    trySend(Result.success(circles))
                } else {
                    trySend(Result.success(emptyList()))
                }
            }

        awaitClose { }
    }

    override fun getCircleDetail(circleId: String): Flow<Result<Circle>> = callbackFlow {
        firestore.collection("circles").document(circleId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val circle = snapshot.toObject(Circle::class.java)
                    if (circle != null) {
                        trySend(Result.success(circle))
                    } else {
                        trySend(Result.failure(Exception("Gagal parsing data circle")))
                    }
                } else {
                    trySend(Result.failure(Exception("Circle tidak ditemukan")))
                }
            }
        awaitClose { }
    }

    override fun createJastipSession(session: JastipSession): Flow<Result<Boolean>> = callbackFlow {
        val myUid = firebaseAuth.currentUser?.uid ?: return@callbackFlow
        val newRef = firestore.collection("sessions").document()

        val finalSession = session.copy(
            id = newRef.id,
            creatorId = myUid,
            participantIds = listOf(myUid)
        )

        newRef.set(finalSession)
            .addOnSuccessListener {
                firestore.collection("circles").document(session.circleId).update("isActiveSession", true)
                trySend(Result.success(true))
            }
            .addOnFailureListener { trySend(Result.failure(it)) }
        awaitClose { }
    }

    override fun getCircleSessions(circleId: String): Flow<Result<List<JastipSession>>> = callbackFlow {
        firestore.collection("sessions")
            .whereEqualTo("circleId", circleId)
            // .orderBy("createdAt", Query.Direction.DESCENDING) // Removed to avoid index issues
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("DEBUG_SESSION", "Error ambil data: ${error.message}")
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val sessions = snapshot.toObjects(JastipSession::class.java)
                    Log.d("DEBUG_SESSION", "Ditemukan ${sessions.size} sesi. Data: $sessions")
                    trySend(Result.success(sessions))
                } else {
                    Log.d("DEBUG_SESSION", "Snapshot null")
                    trySend(Result.success(emptyList()))
                }
            }
        awaitClose { }
    }

    override fun getMyCircle(): Flow<Result<List<User>>> = callbackFlow {
        trySend(Result.success(emptyList()))
        awaitClose { }
    }

    override fun getMyJastipSessions(): Flow<Result<List<JastipSession>>> = callbackFlow {
        val myUid = firebaseAuth.currentUser?.uid ?: return@callbackFlow

        firestore.collection("sessions")
            .whereEqualTo("creatorId", myUid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                val sessions = snapshot?.toObjects(JastipSession::class.java) ?: emptyList()
                trySend(Result.success(sessions))
            }
        awaitClose { }
    }

    override fun getSessionOrders(sessionId: String): Flow<Result<List<JastipOrder>>> = callbackFlow {
        firestore.collection("orders")
            .whereEqualTo("sessionId", sessionId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { trySend(Result.failure(error)); return@addSnapshotListener }
                val orders = snapshot?.toObjects(JastipOrder::class.java) ?: emptyList()
                trySend(Result.success(orders))
            }
        awaitClose { }
    }

    override fun updateOrderStatus(orderId: String, newStatus: String): Flow<Result<Boolean>> = callbackFlow {
        firestore.collection("orders").document(orderId)
            .update("status", newStatus)
            .addOnSuccessListener { trySend(Result.success(true)) }
            .addOnFailureListener { trySend(Result.failure(it)) }
        awaitClose { }
    }

    override fun createJastipOrder(order: JastipOrder): Flow<Result<Boolean>> = callbackFlow {
        val myUid = firebaseAuth.currentUser?.uid ?: return@callbackFlow

        firestore.collection("users").document(myUid).get().addOnSuccessListener { userDoc ->
            val myData = userDoc.toObject(User::class.java)

            if (myData != null) {

                val newOrderRef = firestore.collection("orders").document()
                val finalOrder = order.copy(
                    id = newOrderRef.id,
                    requesterId = myUid,
                    requesterName = myData.name,
                    requesterPhotoUrl = myData.photoUrl,
                    status = "pending",
                    timestamp = com.google.firebase.Timestamp.now()
                )

                newOrderRef.set(finalOrder)
                    .addOnSuccessListener {
                        // firestore.collection("sessions").document(order.sessionId).update("totalOrders", FieldValue.increment(1))
                        trySend(Result.success(true))
                    }
                    .addOnFailureListener { trySend(Result.failure(it)) }
            }
        }
        awaitClose { }
    }

    override fun getSessionChatMessages(sessionId: String): Flow<Result<List<ChatMessage>>> = callbackFlow {
        firestore.collection("sessions").document(sessionId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { trySend(Result.failure(error)); return@addSnapshotListener }
                val messages = snapshot?.toObjects(ChatMessage::class.java) ?: emptyList()
                trySend(Result.success(messages))
            }
        awaitClose { }
    }

    override fun sendSessionChatMessage(sessionId: String, messageText: String): Flow<Result<Boolean>> = callbackFlow {
        val myUid = firebaseAuth.currentUser?.uid ?: return@callbackFlow

        firestore.collection("users").document(myUid).get().addOnSuccessListener { doc ->
            val userName = doc.getString("name") ?: "User"

            val newMsgRef = firestore.collection("sessions").document(sessionId).collection("messages").document()
            val chatMessage = ChatMessage(
                id = newMsgRef.id,
                senderId = myUid,
                senderName = userName,
                message = messageText,
                timestamp = com.google.firebase.Timestamp.now()
            )

            newMsgRef.set(chatMessage)
                .addOnSuccessListener { trySend(Result.success(true)) }
                .addOnFailureListener { trySend(Result.failure(it)) }
        }
        awaitClose { }
    }

    override fun getCurrentUserUid(): String? = firebaseAuth.currentUser?.uid

    override fun updateSessionStatus(sessionId: String, newStatus: String): Flow<Result<Boolean>> = callbackFlow {
        firestore.collection("sessions").document(sessionId)
            .update("status", newStatus)
            .addOnSuccessListener { trySend(Result.success(true)) }
            .addOnFailureListener { trySend(Result.failure(it)) }
        awaitClose { }
    }


    override fun toggleRevisionMode(sessionId: String, isRevision: Boolean): Flow<Result<Boolean>> = callbackFlow {
        firestore.collection("sessions").document(sessionId)
            .update("isRevisionMode", isRevision)
            .addOnSuccessListener { trySend(Result.success(true)) }
            .addOnFailureListener { trySend(Result.failure(it)) }
        awaitClose { }
    }
    override fun updateBankAccount(
        bankName: String,
        bankAccountNumber: String,
        bankAccountName: String
    ): Flow<Result<Boolean>> = callbackFlow {
        val myUid = firebaseAuth.currentUser?.uid
        if (myUid == null) {
            trySend(Result.failure(Exception("User belum login")))
            awaitClose { }
            return@callbackFlow
        }
        val updates = hashMapOf<String, Any>(
            "bankName" to bankName,
            "bankAccountNumber" to bankAccountNumber,
            "bankAccountName" to bankAccountName
        )
        firestore.collection("users").document(myUid)
            .update(updates)
            .addOnSuccessListener { trySend(Result.success(true)) }
            .addOnFailureListener { trySend(Result.failure(it)) }

        awaitClose { }
    }

    override fun listenToPaymentsBySessionAndUser(
        sessionId: String,
        userId: String
    ): Flow<Result<List<PaymentInfo>>> = callbackFlow {
        val listener = firestore.collection("payments")
            .whereEqualTo("sessionId", sessionId)
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("AuthRepositoryImpl", "Payment listener error", error)
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                val payments = snapshot?.toObjects(PaymentInfo::class.java) ?: emptyList()
                android.util.Log.d("AuthRepositoryImpl", "Payment listener: sessionId=$sessionId, userId=$userId, found ${payments.size} payments")
                payments.forEach { p ->
                    android.util.Log.d("AuthRepositoryImpl", "Payment: orderId=${p.orderId}, status=${p.status}, amount=${p.amount}")
                }
                trySend(Result.success(payments))
            }
        awaitClose { listener.remove() }
    }


}