//package com.afsar.titipin.data.remote
//
//import android.util.Log
//import com.afsar.titipin.data.model.*
//import com.afsar.titipin.data.remote.api.FirebaseFunctionsApi
//import com.google.firebase.firestore.FirebaseFirestore
//import kotlinx.coroutines.channels.awaitClose
//import kotlinx.coroutines.flow.Flow
//import kotlinx.coroutines.flow.callbackFlow
//import kotlinx.coroutines.flow.flow
//import javax.inject.Inject
//
//class PaymentRepositoryImpl @Inject constructor(
//    private val api: FirebaseFunctionsApi,
//    private val firestore: FirebaseFirestore
//) : PaymentRepository {
//
//    override suspend fun generateSnapToken(
//        sessionId: String,
//        userId: String,
//        amount: Double,
//        userName: String,
//        userEmail: String
//    ): Flow<Result<SnapTokenResponse>> = flow {
//        try {
//            val request = SnapTokenRequest(
//                sessionId = sessionId,
//                userId = userId,
//                amount = amount,
//                userName = userName,
//                userEmail = userEmail
//            )
//
//            Log.d("PaymentRepo", "Requesting snap token: $request")
//            val response = api.generateSnapToken(request)
//
//            if (response.isSuccessful && response.body() != null) {
//                Log.d("PaymentRepo", "Snap token received: ${response.body()}")
//                emit(Result.success(response.body()!!))
//            } else {
//                val errorMsg = "Failed to generate snap token: ${response.code()} - ${response.message()}"
//                Log.e("PaymentRepo", errorMsg)
//                emit(Result.failure(Exception(errorMsg)))
//            }
//        } catch (e: Exception) {
//            Log.e("PaymentRepo", "Error generating snap token", e)
//            emit(Result.failure(e))
//        }
//    }
//
//    override fun getPaymentStatus(orderId: String): Flow<Result<PaymentInfo?>> = callbackFlow {
//        val listener = firestore.collection("payments")
//            .whereEqualTo("orderId", orderId)
//            .limit(1)
//            .addSnapshotListener { snapshot, error ->
//                if (error != null) {
//                    trySend(Result.failure(error))
//                    return@addSnapshotListener
//                }
//
//                if (snapshot != null && !snapshot.isEmpty) {
//                    val payment = snapshot.documents[0].toObject(PaymentInfo::class.java)
//                    trySend(Result.success(payment))
//                } else {
//                    trySend(Result.success(null))
//                }
//            }
//
//        awaitClose { listener.remove() }
//    }
//
//    override fun getSessionPayments(sessionId: String): Flow<Result<List<PaymentInfo>>> = callbackFlow {
//        val listener = firestore.collection("payments")
//            .whereEqualTo("sessionId", sessionId)
//            .addSnapshotListener { snapshot, error ->
//                if (error != null) {
//                    trySend(Result.failure(error))
//                    return@addSnapshotListener
//                }
//
//                val payments = snapshot?.toObjects(PaymentInfo::class.java) ?: emptyList()
//                trySend(Result.success(payments))
//            }
//
//        awaitClose { listener.remove() }
//    }
//
//    override suspend fun disburseFunds(
//        sessionId: String,
//        jastiperId: String
//    ): Flow<Result<DisbursementResponse>> = flow {
//        try {
//            val request = DisbursementRequest(
//                sessionId = sessionId,
//                jastiperId = jastiperId
//            )
//
//            Log.d("PaymentRepo", "Requesting disbursement: $request")
//            val response = api.disburseFunds(request)
//
//            if (response.isSuccessful && response.body() != null) {
//                Log.d("PaymentRepo", "Disbursement response: ${response.body()}")
//                emit(Result.success(response.body()!!))
//            } else {
//                val errorMsg = "Failed to disburse funds: ${response.code()} - ${response.message()}"
//                Log.e("PaymentRepo", errorMsg)
//                emit(Result.failure(Exception(errorMsg)))
//            }
//        } catch (e: Exception) {
//            Log.e("PaymentRepo", "Error disbursing funds", e)
//            emit(Result.failure(e))
//        }
//    }
//}


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

    override suspend fun generateSnapToken(
        sessionId: String,
        userId: String,
        amount: Long, // Sudah Long
        userName: String,
        userEmail: String,
        userPhone: String
    ): Flow<Result<SnapTokenResponse>> = flow {
        try {
            // Mapping ke Object Request (yang sudah ada @SerializedName)
            val request = SnapTokenRequest(
                sessionId = sessionId,
                userId = userId,
                amount = amount,
                userName = userName,
                userEmail = userEmail,
                userPhone = userPhone
            )

            Log.d("PaymentRepo", "Requesting snap token: $request")
            val response = api.generateSnapToken(request)

            if (response.isSuccessful && response.body() != null) {
                Log.d("PaymentRepo", "Snap token received: ${response.body()}")
                emit(Result.success(response.body()!!))
            } else {
                // Handle error message dari backend/Midtrans
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                val errorMsg = "Failed to generate token (${response.code()}): $errorBody"
                Log.e("PaymentRepo", errorMsg)
                emit(Result.failure(Exception(errorMsg)))
            }
        } catch (e: Exception) {
            Log.e("PaymentRepo", "Error generating snap token", e)
            emit(Result.failure(e))
        }
    }

    override fun getPaymentStatus(orderId: String): Flow<Result<PaymentInfo?>> = callbackFlow {
        // Asumsi: "payments" adalah Root Collection (mudah di-query)
        // Jika nested di dalam session, gunakan firestore.collectionGroup("payments")
        val listener = firestore.collection("payments")
            .whereEqualTo("orderId", orderId)
            .limit(1)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    // Aman: Cek !isEmpty sebelum akses index [0]
                    val payment = snapshot.documents[0].toObject(PaymentInfo::class.java)
                    trySend(Result.success(payment))
                } else {
                    // Belum ada data payment (User belum bayar)
                    trySend(Result.success(null))
                }
            }

        awaitClose { listener.remove() }
    }

    override fun getSessionPayments(sessionId: String): Flow<Result<List<PaymentInfo>>> = callbackFlow {
        // Query history pembayaran di sesi tertentu
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

    override suspend fun disburseFunds(
        sessionId: String,
        jastiperId: String,
        bankCode: String,
        accountNumber: String,
        accountName: String
    ): Flow<Result<DisbursementResponse>> = flow {
        try {
            // Sertakan detail bank di request agar Backend tidak bingung
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
                Log.d("PaymentRepo", "Disbursement success: ${response.body()}")
                emit(Result.success(response.body()!!))
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                val errorMsg = "Disbursement Failed (${response.code()}): $errorBody"
                Log.e("PaymentRepo", errorMsg)
                emit(Result.failure(Exception(errorMsg)))
            }
        } catch (e: Exception) {
            Log.e("PaymentRepo", "Error contacting server", e)
            emit(Result.failure(e))
        }
    }
}