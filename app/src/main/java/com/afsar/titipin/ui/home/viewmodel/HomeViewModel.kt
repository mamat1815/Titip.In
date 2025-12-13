package com.afsar.titipin.ui.home.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afsar.titipin.data.model.Circle
import com.afsar.titipin.data.model.Session
import com.afsar.titipin.data.model.User
import com.afsar.titipin.data.remote.AuthRepository
import com.afsar.titipin.data.remote.repository.circle.CircleRepository
import com.afsar.titipin.data.remote.repository.order.OrderRepository
import com.afsar.titipin.data.remote.repository.session.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val circleRepository: CircleRepository,
    private val sessionRepository: SessionRepository, // Tambahkan ini
    private val orderRepository: OrderRepository      // Tambahkan ini
) : ViewModel() {

    var currentUser by mutableStateOf<User?>(null)
        private set

    var myCircles by mutableStateOf<List<Circle>>(emptyList())
        private set

    // Data mentah (Gabungan sesi yang dibuat & diikuti)
    private var allMySessions = listOf<Session>()

    // Data siap pakai untuk UI
    var activeSession by mutableStateOf<Session?>(null)
        private set

    var sessionHistory by mutableStateOf<List<Session>>(emptyList())
        private set

    var isLoading by mutableStateOf(true)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        loadData()
    }

    fun loadData() {
        isLoading = true
        errorMessage = null

        fetchUserProfile()
        loadMyCircles()
        loadCombinedSessions()
    }

    private fun fetchUserProfile() {
        viewModelScope.launch {
            authRepository.getUserProfile().collect { result ->
                result.onSuccess { user ->
                    currentUser = user
                }
                // Jika gagal, biarkan silent atau log error
            }
        }
    }

    private fun loadMyCircles() {
        viewModelScope.launch {
            circleRepository.getMyCircles().collect { result ->
                result.onSuccess { circles ->
                    myCircles = circles
                }
            }
        }
    }

    // Logika Utama: Menggabungkan Sesi Buatan Sendiri + Sesi yang diikuti (ada order)
    private fun loadCombinedSessions() {
        viewModelScope.launch {
            val uid = authRepository.getCurrentUserUid()
            if (uid == null) {
                isLoading = false
                return@launch
            }

            val tempSessions = mutableListOf<Session>()

            // 1. Ambil Sesi yang SAYA BUAT (Creator)
            // Asumsi: Anda punya fungsi getSessionsByCreator di SessionRepo.
            // Jika tidak, authRepository.getMyJastipSessions() biasanya melakukan ini.
            authRepository.getMyJastipSessions().collect { result ->
                result.onSuccess { createdSessions ->
                    tempSessions.addAll(createdSessions)
                }
            }

            // 2. Ambil Sesi di mana SAYA ORDER (Participant)
            // a. Ambil semua order saya
            val myOrdersResult = orderRepository.getMyOrders(uid).firstOrNull()

            myOrdersResult?.onSuccess { orders ->
                // b. Ambil ID sesi unik dari order tersebut
                val participatedSessionIds = orders.map { it.sessionId }.distinct()

                // c. Ambil detail sesinya satu per satu (atau pakai query whereIn jika ada)
                participatedSessionIds.forEach { sessionId ->
                    // Cek agar tidak duplikat dengan yang sudah diambil di langkah 1
                    if (tempSessions.none { it.id == sessionId }) {
                        val sessionResult = sessionRepository.getSessionById(sessionId).firstOrNull()
                        sessionResult?.onSuccess { session ->
                            tempSessions.add(session)
                        }
                    }
                }
            }

            // 3. Simpan ke variabel dan urutkan
            allMySessions = tempSessions.distinctBy { it.id } // Pastikan unik
                .sortedByDescending { it.createdAt?.toDate()?.time ?: 0L }

            // 4. Pisahkan untuk UI (Active vs History)
            processSessionsForUi()

            isLoading = false
        }
    }

    private fun processSessionsForUi() {
        // Active Session: Status Open, ambil yang paling baru
        activeSession = allMySessions
            .filter { it.status == "open" }
            .maxByOrNull { it.createdAt?.toDate()?.time ?: 0L }

        // History: Status Closed/Cancelled/Done
        sessionHistory = allMySessions
            .filter { it.status == "closed" || it.status == "cancelled" }
            .sortedByDescending { it.createdAt?.toDate()?.time ?: 0L }
            .take(5) // Ambil 5 terakhir saja untuk beranda
    }
}