package com.afsar.titipin.ui.home.viewmodel

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afsar.titipin.data.model.ChatMessage
import com.afsar.titipin.data.model.Order
import com.afsar.titipin.data.model.PaymentInfo
import com.afsar.titipin.data.model.Session
import com.afsar.titipin.data.model.User
import com.afsar.titipin.data.remote.AuthRepository
import com.afsar.titipin.data.remote.PaymentRepository
import com.afsar.titipin.data.remote.repository.order.OrderRepository
import com.afsar.titipin.data.remote.repository.session.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.ceil

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val authRepository: AuthRepository,       // Ganti nama variable agar jelas
    private val paymentRepository: PaymentRepository,
    private val sessionRepository: SessionRepository, // Tambahkan ini
    private val orderRepository: OrderRepository      // Tambahkan ini
) : ViewModel() {

    // --- State Variables ---
    var mySessions by mutableStateOf<List<Session>>(emptyList())
    var currentSession by mutableStateOf<Session?>(null)
    var orders = mutableStateListOf<Order>()
    var chatMessages = mutableStateListOf<ChatMessage>()

    // UI Inputs
    var chatInput by mutableStateOf("")
    var orderItemName by mutableStateOf("")
    var orderQuantity by mutableIntStateOf(1)
    var orderNotes by mutableStateOf("")
    var orderPriceEstimate by mutableStateOf("")

    // Timer & Status
    var timeString by mutableStateOf("00:00")
    var isRevisionPhase by mutableStateOf(false)
    var isSessionExpired by mutableStateOf(false)
    private var timerJob: Job? = null

    // User & Payment
    var currentUserId by mutableStateOf("")
    var currentUser by mutableStateOf<User?>(null)
        private set
    var myPaymentStatus by mutableStateOf<String>("pending")
        private set

    // Messages
    var uiMessage by mutableStateOf<String?>(null)

    // Disbursement State
    var disbursementStatus by mutableStateOf<String?>(null)
    var disbursementMessage by mutableStateOf<String?>(null)
    var disbursementAmount by mutableStateOf<Double>(0.0)

    // Validation Data
    private var sessionPayments = mutableStateListOf<PaymentInfo>()

    companion object {
        private const val PAYMENT_FEE_PERCENTAGE = 0.029
        private const val PAYMENT_FEE_FIXED = 2000.0
        private const val DISBURSEMENT_FEE = 2000.0
    }

    init {
        currentUserId = authRepository.getCurrentUserUid() ?: ""
        fetchUserProfile()
        loadMySessions()
    }

    // --- DATA LOADING ---

    private fun fetchUserProfile() {
        viewModelScope.launch {
            authRepository.getUserProfile().collect { result ->
                result.onSuccess { user ->
                    currentUser = user
                }
            }
        }
    }

    fun loadMySessions() {
        viewModelScope.launch {
            // Asumsi getMyJastipSessions masih ada di AuthRepository
            authRepository.getMyJastipSessions().collect { result ->
                result.onSuccess { mySessions = it }
            }
        }
    }

    fun loadSessionDetail(session: Session) {
        currentSession = session
        isSessionExpired = session.status != "open"
        isRevisionPhase = session.isRevisionMode

        startTimer(session)

        // Load Orders (Pindah ke OrderRepository)
        viewModelScope.launch {
            orderRepository.getOrdersBySession(session.id).collect { result ->
                result.onSuccess {
                    orders.clear()
                    orders.addAll(it)
                }
            }
        }

        // Load Chat (Pindah ke SessionRepository)
        viewModelScope.launch {
            sessionRepository.getSessionChatMessages(session.id).collect { messages ->
                chatMessages.clear()
                chatMessages.addAll(messages)
            }
        }

        // Realtime Revision Mode Update (Pindah ke SessionRepository)
        viewModelScope.launch {
            // Note: Idealnya listen single session, tapi pakai list juga oke
            sessionRepository.getListSession(session.circleId).collect { result ->
                result.onSuccess { sessions ->
                    val updatedSession = sessions.find { it.id == session.id }
                    if (updatedSession != null) {
                        currentSession = updatedSession
                        isRevisionPhase = updatedSession.isRevisionMode
                    }
                }
            }
        }

        // Realtime Payment Status (My Status)
        viewModelScope.launch {
            sessionRepository.listenToPaymentsBySessionAndUser(session.id, currentUserId).collect { result ->
                result.onSuccess { payments ->
                    val myPayment = payments.firstOrNull()
                    myPaymentStatus = myPayment?.status ?: "pending"
                }
            }
        }

        // Realtime All Payments (For Jastiper Validation)
        viewModelScope.launch {
            paymentRepository.getSessionPayments(session.id).collect { result ->
                result.onSuccess { payments ->
                    sessionPayments.clear()
                    sessionPayments.addAll(payments)
                }
            }
        }
    }

    // --- CALCULATIONS (Derived State) ---
    // (Bagian ini tidak berubah, logika matematikanya sudah benar)

    val totalItems by derivedStateOf { orders.filter { it.status == "accepted" || it.status == "bought" }.sumOf { it.quantity } }
    val totalPrice by derivedStateOf { orders.filter { it.status == "accepted" || it.status == "bought" }.sumOf { it.priceEstimate * it.quantity } }
    val userBills by derivedStateOf {
        orders.filter { it.status == "accepted" || it.status == "bought" }
            .groupBy { it.requesterName }
            .mapValues { it.value.sumOf { o -> o.priceEstimate * o.quantity } }
    }
    val myTotalBill by derivedStateOf {
        orders.filter { it.requesterId == currentUserId && (it.status == "accepted" || it.status == "bought") }.sumOf { it.priceEstimate * it.quantity }
    }
    private fun calculatePaymentFee(amount: Double): Double {
        val percentageFee = amount * PAYMENT_FEE_PERCENTAGE
        return ceil(percentageFee + PAYMENT_FEE_FIXED)
    }
    val myPaymentFee by derivedStateOf { calculatePaymentFee(myTotalBill) }
    val myTotalWithFee by derivedStateOf { myTotalBill + myPaymentFee }
    val isPaymentRequired by derivedStateOf { currentSession?.status == "closed" && myTotalBill > 0.0 }
    val canDisburse by derivedStateOf {
        val session = currentSession ?: return@derivedStateOf false
        val isCreator = session.creatorId == currentUserId
        val isClosed = session.status == "closed"
        if (!isCreator || !isClosed) return@derivedStateOf false
        val expectedUserIds = userBills.keys.mapNotNull { userName -> orders.find { it.requesterName == userName }?.requesterId }.distinct()
        val paidUserIds = sessionPayments.filter { it.status == "success" }.map { it.userId }.distinct()
        expectedUserIds.all { it in paidUserIds }
    }
    val totalPaymentFees by derivedStateOf { sessionPayments.filter { it.status == "success" }.sumOf { calculatePaymentFee(it.amount) } }
    val netDisbursementAmount by derivedStateOf {
        val gross = totalPrice
        val fees = totalPaymentFees + DISBURSEMENT_FEE
        maxOf(0.0, gross - fees)
    }

    // --- TIMER LOGIC ---
    private fun startTimer(session: Session) {
        timerJob?.cancel()
        val createdAtTime = session.createdAt?.toDate()?.time ?: System.currentTimeMillis()
        val durationMillis = session.durationMinutes * 60 * 1000L
        val endTime = createdAtTime + durationMillis

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
                    isSessionExpired = false
                } else {
                    timeString = "Waktu Habis"
                    isSessionExpired = true
                    break
                }
                delay(1000)
            }
        }
    }

    // --- ORDER ACTIONS ---

    fun createOrder(onSuccess: () -> Unit) {
        val session = currentSession ?: return

        if (isSessionExpired) { uiMessage = "Maaf, sesi titipan sudah berakhir."; return }

        // Validasi Kuota
        val currentRequesters = orders.map { it.requesterId }.distinct()
        val isImAlreadyIn = currentRequesters.contains(currentUserId)
        if (!isImAlreadyIn && currentRequesters.size >= session.maxTitip) {
            uiMessage = "Kuota penitip sudah penuh (${session.maxTitip} orang)."
            return
        }

        if (orderItemName.isBlank()) { uiMessage = "Nama barang harus diisi."; return }

        val newOrder = Order(
            sessionId = session.id,
            circleId = session.circleId, // PERBAIKAN: Tambahkan circleId
            itemName = orderItemName,
            quantity = orderQuantity,
            notes = orderNotes,
            priceEstimate = orderPriceEstimate.toDoubleOrNull() ?: 0.0
        )

        viewModelScope.launch {
            // PERBAIKAN: Gunakan orderRepository dan parameter circleId
            orderRepository.createOrder(session.circleId, session.id, newOrder).collect { result ->
                result.onSuccess {
                    orderItemName = ""
                    orderQuantity = 1
                    orderNotes = ""
                    orderPriceEstimate = ""
                    onSuccess()
                }
                result.onFailure { uiMessage = "Gagal membuat pesanan: ${it.message}" }
            }
        }
    }

    fun updateOrderStatus(orderId: String, newStatus: String) {
        val session = currentSession ?: return
        viewModelScope.launch {
            // PERBAIKAN: Gunakan orderRepository
            orderRepository.updateOrderStatus(session.circleId, session.id, orderId, newStatus).collect { }
        }
    }

    fun toggleItemBought(order: Order) {
        val newStatus = if (order.status == "bought") "accepted" else "bought"
        updateOrderStatus(order.id, newStatus)
    }

    fun flagItemForRevision(order: Order) {
        updateOrderStatus(order.id, "revision")
        sendChatSystem(order.sessionId, "⚠️ Stok untuk '${order.itemName}' kosong/bermasalah. Mohon konfirmasi pengganti.")
    }

    // --- CHAT ACTIONS ---

    fun sendChat() {
        val session = currentSession ?: return
        if (chatInput.isNotBlank()) {
            val textToSend = chatInput; chatInput = ""
            viewModelScope.launch {
                // PERBAIKAN: Gunakan sessionRepository dan tambah parameter circleId
                sessionRepository.sendSessionChatMessage(session.circleId, session.id, textToSend).collect { }
            }
        }
    }

    private fun sendChatSystem(sessionId: String, msg: String) {
        val session = currentSession ?: return
        viewModelScope.launch {
            sessionRepository.sendSessionChatMessage(session.circleId, sessionId, msg).collect{}
        }
    }

    // --- SESSION CONTROL ---

    fun finishSession() {
        val session = currentSession ?: return
        val closedSession = session.copy(status = "closed")
        viewModelScope.launch {
            // PERBAIKAN: Gunakan sessionRepository.updateSession
            sessionRepository.updateSession(session.circleId, session.id, closedSession).collect { result ->
                result.onSuccess {
                    isSessionExpired = true
                    timeString = "Selesai"
                    loadSessionDetail(closedSession)
                }
            }
        }
    }

    fun toggleRevisionMode() {
        val session = currentSession ?: return
        val newRevisionMode = !session.isRevisionMode
        val updatedSession = session.copy(isRevisionMode = newRevisionMode)

        viewModelScope.launch {
            // PERBAIKAN: Gunakan updateSession untuk mengganti flag
            sessionRepository.updateSession(session.circleId, session.id, updatedSession).collect { result ->
                result.onSuccess {
                    isRevisionPhase = newRevisionMode
                    val msg = if (newRevisionMode) "🔄 MODE REVISI DIAKTIFKAN!" else "✅ Mode revisi selesai."
                    sendChatSystem(session.id, msg)
                }
            }
        }
    }

    fun clearMessage() { uiMessage = null }

    // --- DISBURSEMENT (PENCAIRAN DANA) ---

    fun requestDisbursement() {
        val session = currentSession ?: return

        if (session.creatorId != currentUserId) { disbursementMessage = "Hanya Jastiper yang bisa cairkan dana"; return }
        if (session.status != "closed") { disbursementMessage = "Session harus ditutup dulu"; return }
        if (!canDisburse) { disbursementMessage = "Tunggu semua user bayar dulu"; return }

        val bank = currentUser?.bank
        if (bank == null || bank.bankCode.isEmpty() || bank.bankAccountNumber.isEmpty()) {
            disbursementMessage = "⚠️ Data rekening belum lengkap. Silakan atur di menu Profil."
            disbursementStatus = "failed"
            return
        }

        val totalAmount = sessionPayments.filter { it.status == "success" }.sumOf { it.amount }
        if (totalAmount <= 0.0) { disbursementMessage = "Tidak ada pembayaran yang berhasil"; return }

        disbursementStatus = "loading"
        disbursementMessage = null

        viewModelScope.launch {
            paymentRepository.disburseFunds(
                sessionId = session.id,
                jastiperId = currentUserId,
                bankCode = bank.bankCode,
                accountNumber = bank.bankAccountNumber,
                accountName = bank.bankAccountName
            ).collect { result ->
                result.onSuccess { response ->
                    disbursementStatus = "success"
                    disbursementAmount = response.netAmount
                    disbursementMessage = "Dana berhasil dicairkan: Rp ${response.netAmount.toLong()}"
                }.onFailure { error ->
                    disbursementStatus = "failed"
                    disbursementMessage = "Gagal cairkan dana: ${error.message}"
                }
            }
        }
    }

    fun clearDisbursementMessage() { disbursementMessage = null }
    fun retryDisbursement() { disbursementStatus = null; disbursementMessage = null; requestDisbursement() }
}