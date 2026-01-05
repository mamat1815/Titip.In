package com.afsar.titipin.data.remote.profile

import android.util.Log
import com.afsar.titipin.data.model.Bank
import com.afsar.titipin.data.model.Stats
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ProfileRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val userId get() = auth.currentUser?.uid

    // ... (updateBankAccount & updateProfile tetap sama) ...
    fun updateBankAccount(bank: Bank): Flow<Result<Boolean>> = callbackFlow {
        val uid = userId ?: run { trySend(Result.failure(Exception("No user"))); close(); return@callbackFlow }
        firestore.collection("users").document(uid).update("bank", bank)
            .addOnSuccessListener { trySend(Result.success(true)) }
            .addOnFailureListener { trySend(Result.failure(it)) }
        awaitClose()
    }

    fun updateProfile(name: String, username: String, phone: String): Flow<Result<Boolean>> = callbackFlow {
        val uid = userId ?: run { trySend(Result.failure(Exception("No user"))); close(); return@callbackFlow }
        val updates = mapOf("name" to name, "username" to username, "phone" to phone)
        firestore.collection("users").document(uid).update(updates)
            .addOnSuccessListener { trySend(Result.success(true)) }
            .addOnFailureListener { trySend(Result.failure(it)) }
        awaitClose()
    }

    // --- FETCH STATS (DEBUG MODE) ---
    // 3. FETCH STATISTIK (FIXED: Support Sub-collection)
    suspend fun fetchUserStats(): Stats {
        val uid = userId ?: return Stats()
        Log.d("DEBUG_STATS", "Mulai fetch stats untuk UID: $uid")

        try {
            // PERBAIKAN 1: Gunakan collectionGroup("sessions")
            // Ini akan mencari semua 'sessions' baik yang Public maupun yang di dalam Circle
            val sessionsSnapshot = firestore.collectionGroup("sessions")
                .whereEqualTo("creatorId", uid)
                .get().await()

            val totalSesi = sessionsSnapshot.size()
            Log.d("DEBUG_STATS", "Ditemukan $totalSesi sesi.")

            // PERBAIKAN 2: Gunakan collectionGroup("orders")
            // Jaga-jaga jika orders juga tersimpan di dalam sub-collection session
            val ordersSnapshot = firestore.collectionGroup("orders")
                .whereEqualTo("requesterId", uid)
                .get().await()

            val totalTitip = ordersSnapshot.size()
            Log.d("DEBUG_STATS", "Ditemukan $totalTitip order.")

            var totalExpense = 0.0
            for (doc in ordersSnapshot.documents) {
                val status = doc.getString("status")?.lowercase() ?: ""

                // Hitung pengeluaran (Status sukses)
                if (status == "accepted" || status == "bought" || status == "completed") {
                    val price = doc.getDouble("totalEstimate") ?: 0.0
                    val fee = doc.getDouble("jastipFee") ?: 0.0
                    totalExpense += (price + fee)
                }
            }

            // PERBAIKAN 3: Logika Pemasukan (Income)
            var totalIncome = 0.0

            // Loop setiap sesi yang saya buat
            for (session in sessionsSnapshot.documents) {
                // Karena orders kemungkinan adalah sub-collection dari session ini,
                // kita akses langsung lewat referensi dokumennya.
                val sessionOrders = session.reference.collection("orders")
                    .get().await()

                for (doc in sessionOrders.documents) {
                    val status = doc.getString("status")?.lowercase() ?: ""
                    val requesterId = doc.getString("requesterId")

                    // Hitung jika order sukses DAN bukan punya saya sendiri
                    if (requesterId != uid && (status == "accepted" || status == "bought" || status == "completed")) {
                        val price = doc.getDouble("totalEstimate") ?: 0.0
                        val fee = doc.getDouble("jastipFee") ?: 0.0
                        totalIncome += (price + fee)
                    }
                }
            }

            // 4. Hitung Circle (Tetap sama karena circle biasanya di root)
            // Tapi kalau circle pakai member array, pastikan logicnya benar.
            // Jika members adalah subcollection, logic harus disesuaikan.
            // Asumsi: 'members' adalah Array Field di dalam dokumen circle.
            val circlesSnapshot = firestore.collection("circles")
                .whereArrayContains("members", uid)
                .get().await()
            val totalCircle = circlesSnapshot.size()

            Log.d("DEBUG_STATS", "Final -> Sesi: $totalSesi, Titip: $totalTitip, Inc: $totalIncome")

            return Stats(
                totalTitip = totalTitip,
                totalSesi = totalSesi,
                totalCircle = totalCircle,
                totalIncome = totalIncome,
                totalExpense = totalExpense
            )

        } catch (e: Exception) {
            Log.e("DEBUG_STATS", "ERROR CRITICAL: ${e.message}", e)
            return Stats()
        }
    }
}