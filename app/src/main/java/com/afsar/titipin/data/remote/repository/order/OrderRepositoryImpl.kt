package com.afsar.titipin.data.remote.repository.order

import com.afsar.titipin.data.model.Order
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

class OrderRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : OrderRepository {

    private fun getOrdersCollection(circleId: String, sessionId: String) =
        firestore.collection("circles")
            .document(circleId)
            .collection("sessions")
            .document(sessionId)
            .collection("orders")

    override fun createOrder(circleId: String, sessionId: String, order: Order): Flow<Result<Boolean>> = callbackFlow {
        try {
            // Path: circles/{circleId}/sessions/{sessionId}/orders/{orderId}
            val ref = firestore.collection("circles").document(circleId)
                .collection("sessions").document(sessionId)
                .collection("orders").document()

            // Isi ID otomatis
            val finalOrder = order.copy(id = ref.id)

            ref.set(finalOrder).await()

            trySend(Result.success(true))
            close()
        } catch (e: Exception) {
            trySend(Result.failure(e))
            close()
        }
        awaitClose { }
    }

    override fun getListOrder(
        circleId: String,
        sessionId: String
    ): Flow<Result<List<Order>>> = callbackFlow {
        val listener = getOrdersCollection(circleId, sessionId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val orders = snapshot.toObjects(Order::class.java)
                    trySend(Result.success(orders))
                } else {
                    trySend(Result.success(emptyList()))
                }
            }

        awaitClose { listener.remove() }
    }

    override fun getOrderById(
        circleId: String,
        sessionId: String,
        orderId: String
    ): Flow<Result<Order>> = callbackFlow {
        val listener = getOrdersCollection(circleId, sessionId)
            .document(orderId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val order = snapshot.toObject(Order::class.java)
                    if (order != null) {
                        trySend(Result.success(order))
                    } else {
                        trySend(Result.failure(Exception("Parse error")))
                    }
                } else {
                    trySend(Result.failure(Exception("Order not found")))
                }
            }
        awaitClose { listener.remove() }
    }

    override fun updateOrder(
        circleId: String,
        sessionId: String,
        orderId: String,
        order: Order
    ): Flow<Result<Boolean>> = callbackFlow {
        getOrdersCollection(circleId, sessionId)
            .document(orderId)
            .set(order, SetOptions.merge())
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

    override fun updateOrderStatus(
        circleId: String,
        sessionId: String,
        orderId: String,
        status: String
    ): Flow<Result<Boolean>> = callbackFlow {
        // Update status
        firestore.collection("circles").document(circleId)
            .collection("sessions").document(sessionId)
            .collection("orders").document(orderId)
            .update("status", status)
            .addOnSuccessListener { trySend(Result.success(true)); close() }
            .addOnFailureListener { trySend(Result.failure(it)); close() }

        awaitClose { }
    }

    override fun deleteOrder(
        circleId: String,
        sessionId: String,
        orderId: String
    ): Flow<Result<Boolean>> = callbackFlow {
        getOrdersCollection(circleId, sessionId)
            .document(orderId)
            .delete()
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

    override fun getOrdersBySession(sessionId: String): Flow<Result<List<Order>>> = callbackFlow {
        // Gunakan Collection Group agar tidak perlu circleId
        val listener = firestore.collectionGroup("orders")
            .whereEqualTo("sessionId", sessionId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                val orders = snapshot?.toObjects(Order::class.java) ?: emptyList()
                trySend(Result.success(orders))
            }
        awaitClose { listener.remove() }
    }

    override fun getMyOrders(userId: String): Flow<Result<List<Order>>> = callbackFlow {
        // Menggunakan collectionGroup untuk mencari di semua sub-collection bernama "orders"
        val listener = firestore.collectionGroup("orders")
            .whereEqualTo("requesterId", userId)
            // Opsional: Urutkan berdasarkan waktu (butuh index composite nanti)
            // .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }

                val orders = snapshot?.toObjects(Order::class.java) ?: emptyList()
                trySend(Result.success(orders))
            }
        awaitClose { listener.remove() }
    }
}