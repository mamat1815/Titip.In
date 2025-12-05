package com.afsar.titipin.ui.buy

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afsar.titipin.data.model.ChatMessage
import com.afsar.titipin.data.model.JastipOrder
import com.afsar.titipin.data.model.JastipSession
import com.afsar.titipin.data.remote.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TitipankuViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    var mySessions by mutableStateOf<List<JastipSession>>(emptyList())
    var currentSession by mutableStateOf<JastipSession?>(null)
    var orders = mutableStateListOf<JastipOrder>()
    var chatMessages = mutableStateListOf<ChatMessage>()
    var chatInput by mutableStateOf("")
    var timeString by mutableStateOf("00:00")
    var isRevisionPhase by mutableStateOf(false)
    var isSessionExpired by mutableStateOf(false)
    private var timerJob: Job? = null
    var orderItemName by mutableStateOf("")
    var orderQuantity by mutableIntStateOf(1)
    var orderNotes by mutableStateOf("")
    var orderPriceEstimate by mutableStateOf("")
    var currentUserId by mutableStateOf("")

    var uiMessage by mutableStateOf<String?>(null)

    init {
        currentUserId = repository.getCurrentUserUid() ?: ""
    }

    val totalItems by derivedStateOf { orders.filter { it.status == "accepted" || it.status == "bought" }.sumOf { it.quantity } }
    val totalPrice by derivedStateOf { orders.filter { it.status == "accepted" || it.status == "bought" }.sumOf { it.priceEstimate * it.quantity } }
    val userBills by derivedStateOf { orders.filter { it.status == "accepted" || it.status == "bought" }.groupBy { it.requesterName }.mapValues { it.value.sumOf { o -> o.priceEstimate * o.quantity } } }


    fun loadMySessions() {
        viewModelScope.launch { repository.getMyJastipSessions().collect { result -> result.onSuccess { mySessions = it } } }
    }

    fun loadSessionDetail(session: JastipSession) {
        currentSession = session
        isSessionExpired = session.status != "open"

        startTimer(session)
        viewModelScope.launch { repository.getSessionOrders(session.id).collect { result -> result.onSuccess { orders.clear(); orders.addAll(it) } } }
        viewModelScope.launch { repository.getSessionChatMessages(session.id).collect { result -> result.onSuccess { chatMessages.clear(); chatMessages.addAll(it) } } }
    }

    private fun startTimer(session: JastipSession) {
        timerJob?.cancel()

        val durationMillis = session.durationMinutes * 60 * 1000L
        val endTime = session.createdAt.toDate().time + durationMillis
        val revisionStart = endTime - (2 * 60 * 1000)

        timerJob = viewModelScope.launch {
            while (true) {
                val now = System.currentTimeMillis()
                val timeLeft = endTime - now

                if (session.status != "open") {
                    timeString = "Selesai"
                    isSessionExpired = true
                    break
                }

                if (timeLeft > 0) {
                    val minutes = (timeLeft / 1000) / 60
                    val seconds = (timeLeft / 1000) % 60
                    timeString = String.format("%02d:%02d", minutes, seconds)
                    isRevisionPhase = now >= revisionStart
                    isSessionExpired = false
                } else {
                    timeString = "Waktu Habis"
                    isRevisionPhase = false
                    isSessionExpired = true
                    // Optional: Auto close session di DB
                    break
                }
                delay(1000)
            }
        }
    }

    fun createOrder(onSuccess: () -> Unit) {
        val session = currentSession ?: return

        if (isSessionExpired) {
            uiMessage = "Maaf, sesi titipan sudah berakhir."
            return
        }

        val currentRequesters = orders.map { it.requesterId }.distinct()
        val isImAlreadyIn = currentRequesters.contains(currentUserId)

        if (!isImAlreadyIn && currentRequesters.size >= session.maxTitip) {
            uiMessage = "Kuota penitip sudah penuh (${session.maxTitip} orang)."
            return
        }

        if (orderItemName.isBlank()) {
            uiMessage = "Nama barang harus diisi."
            return
        }

        val priceDouble = orderPriceEstimate.toDoubleOrNull() ?: 0.0

        val newOrder = JastipOrder(
            sessionId = session.id,
            itemName = orderItemName,
            quantity = orderQuantity,
            notes = orderNotes,
            priceEstimate = priceDouble
        )

        viewModelScope.launch {
            repository.createJastipOrder(newOrder).collect { result ->
                result.onSuccess {
                    orderItemName = ""
                    orderQuantity = 1
                    orderNotes = ""
                    orderPriceEstimate = ""
                    onSuccess()
                }
                result.onFailure { uiMessage = "Gagal membuat pesanan." }
            }
        }
    }

    // --- UPDATE STATUS ---
    fun updateOrderStatus(orderId: String, newStatus: String) {
        viewModelScope.launch { repository.updateOrderStatus(orderId, newStatus).collect { } }
    }

    fun toggleItemBought(order: JastipOrder) {
        val newStatus = if (order.status == "bought") "accepted" else "bought"
        updateOrderStatus(order.id, newStatus)
    }

    fun flagItemForRevision(order: JastipOrder) {
        updateOrderStatus(order.id, "revision")
        // Otomatis kirim chat notifikasi
        val msg = "⚠️ Stok untuk '${order.itemName}' kosong/bermasalah. Mohon konfirmasi pengganti."
        sendChatSystem(order.sessionId, msg)
    }

    fun sendChat() {
        val session = currentSession ?: return
        if (chatInput.isNotBlank()) {
            val textToSend = chatInput; chatInput = ""
            viewModelScope.launch { repository.sendSessionChatMessage(session.id, textToSend).collect { } }
        }
    }

    // Helper kirim chat sistem
    private fun sendChatSystem(sessionId: String, msg: String) {
        viewModelScope.launch { repository.sendSessionChatMessage(sessionId, msg).collect{} }
    }

    fun finishSession() {
        val session = currentSession ?: return
        viewModelScope.launch {
            repository.updateSessionStatus(session.id, "closed").collect { result ->
                result.onSuccess {
                    isSessionExpired = true
                    timeString = "Selesai"
                    // Refresh session data
                    loadSessionDetail(session.copy(status = "closed"))
                }
            }
        }
    }

    fun clearMessage() { uiMessage = null }
}