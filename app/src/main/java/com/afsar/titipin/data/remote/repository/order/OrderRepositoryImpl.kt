package com.afsar.titipin.data.remote.repository.order

import android.util.Log
import com.afsar.titipin.data.model.Order
import com.afsar.titipin.data.model.OrderItem
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

    // --- FUNGSI CREATE ORDER (LOGIKA CEK DUPLIKASI TETAP DIPERTAHANKAN) ---
    override fun createOrder(circleId: String, sessionId: String, order: Order): Flow<Result<Boolean>> = callbackFlow {
        try {
            val ordersRef = getOrdersCollection(circleId, sessionId)
            val myUid = order.requesterId

            // 1. Cek apakah user ini sudah punya Order di Sesi ini?
            val querySnapshot = ordersRef
                .whereEqualTo("requesterId", myUid)
                .limit(1)
                .get()
                .await()

            if (!querySnapshot.isEmpty) {
                // A. SUDAH ADA ORDER -> UPDATE (Tambah Item ke Array)
                val existingDoc = querySnapshot.documents[0]
                val existingOrderId = existingDoc.id

                val newItems = order.items
                val currentOrder = existingDoc.toObject(Order::class.java)!!

                // Gabungkan item lama + baru
                val updatedItems = currentOrder.items.toMutableList().apply {
                    addAll(newItems)
                }

                // Hitung ulang total estimate
                val newTotalEstimate = updatedItems.sumOf { it.priceEstimate * it.quantity }

                val updates = mapOf(
                    "items" to updatedItems,
                    "totalEstimate" to newTotalEstimate,
                    "status" to "pending" // Reset status ke pending
                )

                ordersRef.document(existingOrderId).update(updates).await()
                Log.d("OrderRepo", "Updated existing order $existingOrderId with new items")

            } else {
                // B. BELUM ADA ORDER -> BUAT BARU
                val newDocRef = ordersRef.document()
                val initialTotal = order.items.sumOf { it.priceEstimate * it.quantity }

                val finalOrder = order.copy(
                    id = newDocRef.id,
                    totalEstimate = initialTotal,
                    timestamp = null
                )

                newDocRef.set(finalOrder).await()
                Log.d("OrderRepo", "Created NEW order ${newDocRef.id}")
            }

            trySend(Result.success(true))
            close()

        } catch (e: Exception) {
            Log.e("OrderRepo", "Gagal create/update order: ${e.message}")
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

    override fun getOneMyOrder(): Flow<Result<Order>> = callbackFlow {
        val myUid = firebaseAuth.currentUser?.uid
        if (myUid == null) {
            trySend(Result.failure(Exception("User belum login")))
            close()
            return@callbackFlow
        }

        val listener = firestore.collectionGroup("orders")
            .whereEqualTo("requesterId", myUid)
            .limit(1)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val order = snapshot.documents[0].toObject(Order::class.java)
                    if (order != null) {
                        trySend(Result.success(order))
                    }
                } else {
                    trySend(Result.failure(Exception("Belum ada pesanan")))
                }
            }

        awaitClose { listener.remove() }
    }

    override fun getMyOrderList(): Flow<Result<List<Order>>> = callbackFlow {
        val myUid = firebaseAuth.currentUser?.uid
        if (myUid == null) {
            trySend(Result.failure(Exception("User belum login")))
            close()
            return@callbackFlow
        }

        val listener = firestore.collectionGroup("orders")
            .whereEqualTo("requesterId", myUid)
            .orderBy("timestamp", Query.Direction.DESCENDING)
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

    override fun updateOrder(
        circleId: String,
        sessionId: String,
        orderId: String,
        order: Order
    ): Flow<Result<Boolean>> = callbackFlow {
        getOrdersCollection(circleId, sessionId)
            .document(orderId)
            .set(order, SetOptions.merge())
            .addOnSuccessListener { trySend(Result.success(true)); close() }
            .addOnFailureListener { trySend(Result.failure(it)); close() }
        awaitClose { }
    }

    override fun updateOrderStatus(
        circleId: String,
        sessionId: String,
        orderId: String,
        status: String
    ): Flow<Result<Boolean>> = callbackFlow {
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
            .addOnSuccessListener { trySend(Result.success(true)); close() }
            .addOnFailureListener { trySend(Result.failure(it)); close() }
        awaitClose { }
    }

    override fun getOrdersBySession(sessionId: String): Flow<Result<List<Order>>> = callbackFlow {
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

    override fun getMyOrders(): Flow<Result<List<Order>>> = callbackFlow {
        val myUid = firebaseAuth.currentUser?.uid
        val listener = firestore.collectionGroup("orders")
            .whereEqualTo("requesterId", myUid)
            .addSnapshotListener { snapshot, error ->
                val orders = snapshot?.toObjects(Order::class.java) ?: emptyList()
                trySend(Result.success(orders))
            }
        awaitClose { listener.remove() }
    }

    // --- FUNGSI BARU: UPDATE ORDER ITEMS (UNTUK EDIT HARGA/STATUS PER ITEM) ---
    // Fungsi ini mencari dokumen order berdasarkan ID (lewat collectionGroup agar praktis)
    // lalu menimpa field 'items' dengan list baru.
    override fun updateOrderItems(orderId: String, newItems: List<OrderItem>): Flow<Result<Boolean>> = callbackFlow {
        try {
            // 1. Cari dokumennya dulu (karena kita tidak tau circleId & sessionId-nya di sini)
            val query = firestore.collectionGroup("orders")
                .whereEqualTo("id", orderId) // Asumsi field 'id' di dalam dokumen Order = documentId
                .limit(1)
                .get()
                .await()

            if (!query.isEmpty) {
                val doc = query.documents[0]

                // 2. Update field "items"
                doc.reference.update("items", newItems).await()

                trySend(Result.success(true))
            } else {
                trySend(Result.failure(Exception("Order ID $orderId tidak ditemukan di database")))
            }

            close()
        } catch (e: Exception) {
            Log.e("OrderRepo", "Gagal update items: ${e.message}")
            trySend(Result.failure(e))
            close()
        }
        awaitClose { }
    }
}