package com.afsar.titipin.data.remote

import android.util.Log
import com.afsar.titipin.data.model.*
import com.afsar.titipin.data.remote.api.FirebaseFunctionsApi
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class PaymentRepositoryImpl @Inject constructor(
    private val api: FirebaseFunctionsApi,
    private val firestore: FirebaseFirestore
) : PaymentRepository {

    // --- 1. GENERATE TOKEN (Untuk User Bayar) ---
    override suspend fun generateSnapToken(
        sessionId: String,
        userId: String,
        amount: Long, // Subtotal (Harga + Tip) dalam Long (tanpa desimal)
        userName: String,
        userEmail: String,
        userPhone: String
    ): Flow<Result<SnapTokenResponse>> = flow {
        try {
            // Mapping data ke Request Object
            val request = SnapTokenRequest(
                sessionId = sessionId,
                userId = userId,
                amount = amount,
                userName = userName,
                userEmail = userEmail,
                userPhone = userPhone
            )

            Log.d("PaymentRepo", "Requesting snap token: $request")

            // Panggil API Backend
            val response = api.generateSnapToken(request)

            if (response.isSuccessful && response.body() != null) {
                Log.d("PaymentRepo", "Success! Token: ${response.body()?.snapToken}")
                emit(Result.success(response.body()!!))
            } else {
                // Ambil pesan error detail dari backend
                val errorBody = response.errorBody()?.string()
                val errorMsg = "Backend Error (${response.code()}): $errorBody"
                Log.e("PaymentRepo", errorMsg)
                emit(Result.failure(Exception(errorMsg)))
            }
        } catch (e: Exception) {
            Log.e("PaymentRepo", "Network/Client Error", e)
            emit(Result.failure(e))
        }
    }

    // --- 2. CEK STATUS PAYMENT (Realtime Firestore) ---
    override fun getPaymentStatus(orderId: String): Flow<Result<PaymentInfo?>> = callbackFlow {
        // Query ke collection 'payments' berdasarkan orderId
        val listener = firestore.collection("payments")
            .whereEqualTo("orderId", orderId)
            .limit(1)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val payment = snapshot.documents[0].toObject(PaymentInfo::class.java)
                    trySend(Result.success(payment))
                } else {
                    // Data belum ada (belum klik bayar)
                    trySend(Result.success(null))
                }
            }

        awaitClose { listener.remove() }
    }

    // --- 3. LIST PEMBAYARAN SESI (Untuk Jastiper Cek Siapa yang Sudah Bayar) ---
    override fun getSessionPayments(sessionId: String): Flow<Result<List<PaymentInfo>>> = callbackFlow {
        val listener = firestore.collection("payments")
            .whereEqualTo("sessionId", sessionId)
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

    // --- 4. PENCAIRAN DANA (Untuk Jastiper) ---
    override suspend fun disburseFunds(
        sessionId: String,
        jastiperId: String,
        bankCode: String,
        accountNumber: String,
        accountName: String
    ): Flow<Result<DisbursementResponse>> = flow {
        try {
            val request = DisbursementRequest(
                sessionId = sessionId,
                jastiperId = jastiperId,
                bankCode = bankCode,
                accountNumber = accountNumber,
                accountName = accountName
            )

            Log.d("PaymentRepo", "Requesting disbursement: $request")
            val response = api.disburseFunds(request)

            if (response.isSuccessful && response.body() != null) {
                Log.d("PaymentRepo", "Disbursement Success: ${response.body()}")
                emit(Result.success(response.body()!!))
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMsg = "Disbursement Failed (${response.code()}): $errorBody"
                Log.e("PaymentRepo", errorMsg)
                emit(Result.failure(Exception(errorMsg)))
            }
        } catch (e: Exception) {
            Log.e("PaymentRepo", "Network Error", e)
            emit(Result.failure(e))
        }
    }

    override fun getDisbursementBySession(sessionId: String): Flow<Result<DisbursementInfo?>> = callbackFlow {
        val listener = firestore.collection("disbursements")
            .whereEqualTo("sessionId", sessionId)
            .limit(1) // Ambil 1 saja cukup
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    // Ada data pencairan!
                    val disbursement = snapshot.documents[0].toObject(DisbursementInfo::class.java)
                    trySend(Result.success(disbursement))
                } else {
                    // Belum pernah cair
                    trySend(Result.success(null))
                }
            }
        awaitClose { listener.remove() }
    }
}