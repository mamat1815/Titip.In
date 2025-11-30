package com.afsar.titipin.ui.buy

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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

    // --- STATE ---
    var mySessions by mutableStateOf<List<JastipSession>>(emptyList())
    var currentSession by mutableStateOf<JastipSession?>(null)

    // List Pesanan Realtime
    var orders = mutableStateListOf<JastipOrder>()

    // Chat Realtime
    var chatMessages = mutableStateListOf<ChatMessage>()
    var chatInput by mutableStateOf("")

    // Timer
    var timeString by mutableStateOf("00:00")
    var isRevisionPhase by mutableStateOf(false)
    private var timerJob: Job? = null

    var orderItemName by mutableStateOf("")
    var orderQuantity by mutableIntStateOf(1)
    var orderNotes by mutableStateOf("")
    var orderPriceEstimate by mutableStateOf("") // String agar mudah diinput textfield

    // --- STATE BARU: ID USER YANG LOGIN ---
    var currentUserId by mutableStateOf("")

    init {
        // Ambil ID saat ViewModel dibuat
        currentUserId = repository.getCurrentUserUid() ?: ""
    }

    // ... (Fungsi loadSessionDetail, timer, dll TETAP SAMA) ...

    // Helper function untuk cek apakah user adalah pemilik sesi
    fun isSessionCreator(session: JastipSession): Boolean {
        return session.creatorId == currentUserId
    }



    // --- KALKULASI TOTAL (Computed State) ---
    val totalItems by derivedStateOf {
        orders.filter { it.status == "accepted" || it.status == "bought" }.sumOf { it.quantity }
    }

    val totalPrice by derivedStateOf {
        orders.filter { it.status == "accepted" || it.status == "bought" }
            .sumOf { it.priceEstimate * it.quantity }
    }

    // --- KALKULASI PER USER (Bill) ---
    // Mengelompokkan order berdasarkan nama pemesan
    val userBills by derivedStateOf {
        orders.filter { it.status == "accepted" || it.status == "bought" }
            .groupBy { it.requesterName }
            .mapValues { entry ->
                entry.value.sumOf { it.priceEstimate * it.quantity }
            }
    }

    // ... (Fungsi loadSession, Timer, Chat SAMA SEPERTI SEBELUMNYA) ...
    fun loadMySessions() {
        viewModelScope.launch { repository.getMyJastipSessions().collect { result -> result.onSuccess { mySessions = it } } }
    }

    fun loadSessionDetail(session: JastipSession) {
        currentSession = session
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
                if (timeLeft > 0) {
                    val minutes = (timeLeft / 1000) / 60
                    val seconds = (timeLeft / 1000) % 60
                    timeString = String.format("%02d:%02d", minutes, seconds)
                    isRevisionPhase = now >= revisionStart
                } else {
                    timeString = "Selesai"; isRevisionPhase = false; break
                }
                delay(1000)
            }
        }
    }

    fun updateOrderStatus(orderId: String, newStatus: String) {
        viewModelScope.launch { repository.updateOrderStatus(orderId, newStatus).collect { } }
    }

    // --- FUNGSI BARU: TOGGLE CHECKLIST BELANJA ---
    fun toggleItemBought(order: JastipOrder) {
        // Jika status sekarang "accepted" -> ubah jadi "bought" (dicentang)
        // Jika status sekarang "bought" -> ubah jadi "accepted" (batal centang)
        val newStatus = if (order.status == "bought") "accepted" else "bought"
        updateOrderStatus(order.id, newStatus)
    }

    fun sendChat() {
        val session = currentSession ?: return
        if (chatInput.isNotBlank()) {
            val textToSend = chatInput; chatInput = ""
            viewModelScope.launch { repository.sendSessionChatMessage(session.id, textToSend).collect { } }
        }
    }



    // --- FUNGSI BUAT ORDER BARU ---
    fun createOrder(onSuccess: () -> Unit) {
        val session = currentSession ?: return
        if (orderItemName.isBlank()) return

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
                    // Reset Form
                    orderItemName = ""
                    orderQuantity = 1
                    orderNotes = ""
                    orderPriceEstimate = ""
                    onSuccess()
                }
            }
        }
    }

    // --- LOAD DAFTAR SESI (TITIPANKU) ---
//    fun loadMySessions() {
//        viewModelScope.launch {
//            repository.getMyJastipSessions().collect { result ->
//                result.onSuccess { mySessions = it }
//            }
//        }
//    }

//    // --- MASUK KE DETAIL SESI ---
//    fun loadSessionDetail(session: JastipSession) {
//        currentSession = session
//
//        // 1. Jalankan Timer
//        startTimer(session)
//
//        // 2. Listen Orderan Realtime
//        viewModelScope.launch {
//            repository.getSessionOrders(session.id).collect { result ->
//                result.onSuccess {
//                    orders.clear()
//                    orders.addAll(it)
//                }
//            }
//        }
//
//        // 3. Listen Chat Realtime
//        viewModelScope.launch {
//            repository.getSessionChatMessages(session.id).collect { result ->
//                result.onSuccess {
//                    chatMessages.clear()
//                    chatMessages.addAll(it)
//                }
//            }
//        }
//    }

//    // --- TIMER LOGIC (REALTIME SYNC) ---
//    private fun startTimer(session: JastipSession) {
//        timerJob?.cancel()
//        val durationMillis = session.durationMinutes * 60 * 1000L
//        val endTime = session.createdAt.toDate().time + durationMillis
//        val revisionStart = endTime - (2 * 60 * 1000) // 2 menit sebelum habis
//
//        timerJob = viewModelScope.launch {
//            while (true) {
//                val now = System.currentTimeMillis()
//                val timeLeft = endTime - now
//
//                if (timeLeft > 0) {
//                    val minutes = (timeLeft / 1000) / 60
//                    val seconds = (timeLeft / 1000) % 60
//                    timeString = String.format("%02d:%02d", minutes, seconds)
//
//                    // Cek Fase Revisi (Jika waktu < 2 menit)
//                    isRevisionPhase = now >= revisionStart
//                } else {
//                    timeString = "Selesai"
//                    isRevisionPhase = false
//                    break
//                }
//                delay(1000)
//            }
//        }
//    }

//    // --- UPDATE ORDER (ACCEPT/REJECT) ---
//    fun updateOrderStatus(orderId: String, newStatus: String) {
//        viewModelScope.launch {
//            repository.updateOrderStatus(orderId, newStatus).collect {
//                // UI akan otomatis update karena kita pakai snapshot listener di init
//            }
//        }
//    }
//
//    // --- KIRIM CHAT ---
//    fun sendChat() {
//        val session = currentSession ?: return
//        if (chatInput.isNotBlank()) {
//            val textToSend = chatInput
//            chatInput = "" // Clear input dulu biar responsif
//
//            viewModelScope.launch {
//                repository.sendSessionChatMessage(session.id, textToSend).collect {
//                    // Chat akan muncul otomatis via listener
//                }
//            }
//        }
//    }
}