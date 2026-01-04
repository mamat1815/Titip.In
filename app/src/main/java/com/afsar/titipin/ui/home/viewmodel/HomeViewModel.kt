package com.afsar.titipin.ui.home.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afsar.titipin.data.model.Circle
import com.afsar.titipin.data.model.Order
import com.afsar.titipin.data.model.Session
import com.afsar.titipin.data.model.User
import com.afsar.titipin.data.remote.AuthRepository
import com.afsar.titipin.data.remote.repository.circle.CircleRepository
import com.afsar.titipin.data.remote.repository.order.OrderRepository
import com.afsar.titipin.data.remote.repository.session.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val circleRepository: CircleRepository,
    private val sessionRepository: SessionRepository,
    private val orderRepository: OrderRepository
) : ViewModel() {

    var currentUser by mutableStateOf<User?>(null)
        private set

    var myCircles by mutableStateOf<List<Circle>>(emptyList())
        private set

    var myOrderSessionState by mutableStateOf(ActiveSessionUiState())
        private set

    var isSessionLoading by mutableStateOf(false)
        private set

    var activeSessionState by mutableStateOf(ActiveSessionUiState())
        private set

    var sessionHistory by mutableStateOf<List<Session>>(emptyList())
        private set
    var orderHistory by mutableStateOf<List<Order>>(emptyList())
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
        loadActiveSession()
        loadLastOrderSession()
        loadOrderHistory()
    }

    private fun fetchUserProfile() {
        viewModelScope.launch {
            authRepository.getUserProfile().collect { result ->
                result.onSuccess { user ->
                    currentUser = user
                }
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

    private fun loadActiveSession() {
        viewModelScope.launch {
            sessionRepository.getOneMySession().collectLatest { result ->
                result.onSuccess { session ->

                    val userIdsToFetch = listOf(session.creatorId)

                    fetchAvatarsForSession(session, userIdsToFetch)
                }

                result.onFailure {
                    activeSessionState = ActiveSessionUiState(session = null)
                }
            }
        }
    }

    private suspend fun fetchAvatarsForSession(session: Session, userIds: List<String>) {
        authRepository.getUsersByIds(userIds).collect { userResult ->
            userResult.onSuccess { users ->

                val avatarUrls = users.map { it.photoUrl }.filter { it.isNotEmpty() }

                activeSessionState = ActiveSessionUiState(
                    session = session,
                    participantAvatars = avatarUrls
                )
            }
        }
    }
    private fun loadLastOrderSession() {
        viewModelScope.launch {
            Log.d("DEBUG_VM", "1. Start loadLastOrderSession")

            orderRepository.getOneMyOrder().collectLatest { orderResult ->
                orderResult.onSuccess { order ->
                    Log.d("DEBUG_VM", "2. Order Ditemukan! ID: ${order.id}")

                    // --- PERBAIKAN DI SINI ---
                    // Ambil summary dari list items
                    val itemNameDisplay = if (order.items.isNotEmpty()) {
                        val first = order.items.first()
                        if (order.items.size > 1) {
                            "${first.quantity}x ${first.name} (+${order.items.size - 1} lainnya)"
                        } else {
                            "${first.quantity}x ${first.name}"
                        }
                    } else {
                        "Detail barang tidak tersedia"
                    }

                    // Lanjut ambil Session
                    sessionRepository.getSessionById(order.sessionId).collect { sessionResult ->
                        sessionResult.onSuccess { session ->
                            Log.d("DEBUG_VM", "3. Session Ditemukan! Host: ${session.creatorName}")

                            val creatorId = listOf(session.creatorId)
                            authRepository.getUsersByIds(creatorId).collect { userResult ->
                                val avatars = userResult.getOrNull()?.map { it.photoUrl } ?: emptyList()

                                myOrderSessionState = ActiveSessionUiState(
                                    session = session,
                                    participantAvatars = avatars,
                                    orderItemName = itemNameDisplay // String hasil format di atas
                                )
                            }
                        }
                        sessionResult.onFailure { e ->
                            Log.e("DEBUG_VM", "3. GAGAL Fetch Session: ${e.message}")
                        }
                    }
                }
                orderResult.onFailure { e ->
                    Log.e("DEBUG_VM", "2. Gagal/Kosong Order: ${e.message}")
                    myOrderSessionState = ActiveSessionUiState(session = null)
                }
            }
        }
    }
    private fun loadOrderHistory() {
        viewModelScope.launch {
            // Mengambil list order dari repository
            orderRepository.getMyOrderList().collect { result ->
                result.onSuccess { orders ->
                    // Opsional: Filter hanya yang sudah selesai jika mau murni "History"
                    // val historyOnly = orders.filter { it.status == "completed" || it.status == "cancelled" }

                    // Kita ambil semua, lalu ambil 5 terakhir saja untuk preview di Home
                    orderHistory = orders.take(5)
                }
                result.onFailure {
                    // Handle error silent
                }
            }
        }
    }
}
data class ActiveSessionUiState(
    val session: Session? = null,
    val participantAvatars: List<String> = emptyList(),
    val orderItemName: String? = null

)
