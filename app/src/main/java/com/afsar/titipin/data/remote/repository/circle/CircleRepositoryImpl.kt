package com.afsar.titipin.data.remote.repository.circle

import android.util.Log
import com.afsar.titipin.data.model.Circle
import com.afsar.titipin.data.model.User
import com.afsar.titipin.data.remote.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class CircleRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : CircleRepository {

    override fun createCircle(
        name: String,
        members: List<User>
    ): Flow<Result<Boolean>> = callbackFlow {
        val myUid = firebaseAuth.currentUser?.uid
        if (myUid == null) {
            trySend(Result.failure(Exception("User belum login")))
            close()
            return@callbackFlow
        }

        try {
            val memberIdSet = members.map { it.uid }.toMutableSet()

            memberIdSet.add(myUid)

            val finalMemberIds = memberIdSet.toList()

            val newCircleRef = firestore.collection("circles").document()

            val newCircle = Circle(
                id = newCircleRef.id,
                name = name,
                createdBy = myUid,
                memberIds = finalMemberIds,
                activeSessionId = null,
                isActiveSession = false,
                lastMessage = "Grup dibuat",
                lastMessageTime = com.google.firebase.Timestamp.now()
            )

            newCircleRef.set(newCircle)
                .addOnSuccessListener {
                    trySend(Result.success(true))
                    close()
                }
                .addOnFailureListener { e ->
                    trySend(Result.failure(e))
                    close()
                }

        } catch (e: Exception) {
            trySend(Result.failure(e))
            close()
        }

        awaitClose { }
    }

    override fun getMyCircles(): Flow<Result<List<Circle>>> = callbackFlow {
        val myUid = firebaseAuth.currentUser?.uid
        if (myUid == null) {
            trySend(Result.failure(Exception("User belum login")))
            close()
            return@callbackFlow
        }

        val listener = firestore.collection("circles")
            .whereArrayContains("memberIds", myUid)
            .orderBy("lastMessageTime", com.google.firebase.firestore.Query.Direction.DESCENDING) // Urutkan dari chat terakhir
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("CircleRepo", "Error query: ${error.message}")
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    Log.d("CircleRepo", "Ditemukan ${snapshot.size()} circle") // Cek jumlahnya
                    val circles = snapshot.toObjects(Circle::class.java)
                    trySend(Result.success(circles))
                } else {
                    trySend(Result.success(emptyList()))
                }
            }

        awaitClose { listener.remove() }
    }

    override fun getCircleDetail(circleId: String): Flow<Result<Circle>> = callbackFlow {
        val listener = firestore.collection("circles").document(circleId)
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
        awaitClose { listener.remove() }
    }
}