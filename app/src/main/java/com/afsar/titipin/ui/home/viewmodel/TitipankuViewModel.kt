package com.afsar.titipin.ui.home.viewmodel

import android.util.Log
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
import com.afsar.titipin.data.model.PaymentInfo
import com.afsar.titipin.data.model.User
import com.afsar.titipin.data.remote.AuthRepository
import com.afsar.titipin.data.remote.PaymentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.ceil

@HiltViewModel
class TitipankuViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val paymentRepository: PaymentRepository
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

    var currentUser by mutableStateOf<User?>(null)
        private set

    var myPaymentStatus by mutableStateOf<String>("pending")
        private set

    // Disbursement state
    var disbursementStatus by mutableStateOf<String?>(null) // null, "loading", "success", "failed"
        private set
    var disbursementMessage by mutableStateOf<String?>(null)
        private set
    var disbursementAmount by mutableStateOf<Double>(0.0)
        private set

    // Track all session payments for validation
    private var sessionPayments = mutableStateListOf<PaymentInfo>()

    // Fee calculation constants
    companion object {
        private const val PAYMENT_FEE_PERCENTAGE= 0.029  // 2.9%
        private const val PAYMENT_FEE_FIXED = 2000.0
        private const val DISBURSEMENT_FEE = 2000.0
    }

    init {
        currentUserId = repository.getCurrentUserUid() ?: ""
        fetchUserProfile()
    }

    private fun fetchUserProfile() {
        viewModelScope.launch {
            repository.getUserProfile().collect { result ->
                result.onSuccess { user ->
                    currentUser = user
                }
            }
        }
    }

    val totalItems by derivedStateOf {
        orders.filter { it.status == "accepted" || it.status == "bought" }.sumOf { it.quantity }
    }
    val totalPrice by derivedStateOf {
        orders.filter { it.status == "accepted" || it.status == "bought" }
            .sumOf { it.priceEstimate * it.quantity }
    }
    val userBills by derivedStateOf {
        orders.filter { it.status == "accepted" || it.status == "bought" }
            .groupBy { it.requesterName }
            .mapValues { it.value.sumOf { o -> o.priceEstimate * o.quantity } }
    }

    // Payment calculations
    val myTotalBill by derivedStateOf {
        orders.filter {
            it.requesterId == currentUserId &&
                    (it.status == "accepted" || it.status == "bought")
        }.sumOf { it.priceEstimate * it.quantity }
    }

    val isPaymentRequired by derivedStateOf {
        currentSession?.status == "closed" && myTotalBill > 0.0
    }

    // Check if Jastiper can disburse funds
    val canDisburse by derivedStateOf {
        val session = currentSession ?: return@derivedStateOf false
        val isCreator = session.creatorId == currentUserId
        val isClosed = session.status == "closed"

        if (!isCreator || !isClosed) return@derivedStateOf false

        // Validate all users have paid
        val expectedUserIds = userBills.keys.mapNotNull { userName ->
            // Find userId from orders by requesterName
            orders.find { it.requesterName == userName }?.requesterId
        }.distinct()

        val paidUserIds = sessionPayments
            .filter { it.status == "success" }
            .map { it.userId }
            .distinct()

        // All expected users must have paid
        expectedUserIds.all { it in paidUserIds }
    }

    // Calculate payment fee for a single transaction
    private fun calculatePaymentFee(amount: Double): Double {
        val percentageFee = amount * PAYMENT_FEE_PERCENTAGE
        return ceil(percentageFee + PAYMENT_FEE_FIXED)
    }

    // Payment fee for current user's bill (before payment)
    val myPaymentFee by derivedStateOf {
        calculatePaymentFee(myTotalBill)
    }

    // Total amount current user needs to pay (including fee)
    val myTotalWithFee by derivedStateOf {
        myTotalBill + myPaymentFee
    }

    // Current user's name for display
    val currentUserName: String
        get() = currentUser?.name ?: "Unknown"

    // Total payment fees for all successful payments (for disbursement calculation)
    val totalPaymentFees by derivedStateOf {
        sessionPayments
            .filter { it.status == "success" }
            .sumOf { calculatePaymentFee(it.amount) }
    }

    // Net amount after deducting all fees
    val netDisbursementAmount by derivedStateOf {
        val gross = totalPrice
        val fees = totalPaymentFees + DISBURSEMENT_FEE
        maxOf(0.0, gross - fees)  // Ensure non-negative
    }


    fun loadMySessions() {
        viewModelScope.launch { repository.getMyJastipSessions().collect { result -> result.onSuccess { mySessions = it } } }
    }

    fun loadSessionDetail(session: JastipSession) {
        currentSession = session
        isSessionExpired = session.status != "open"
        isRevisionPhase = session.isRevisionMode

        startTimer(session)
        viewModelScope.launch { repository.getSessionOrders(session.id).collect { result -> result.onSuccess { orders.clear(); orders.addAll(it) } } }
        viewModelScope.launch { repository.getSessionChatMessages(session.id).collect { result -> result.onSuccess { chatMessages.clear(); chatMessages.addAll(it) } } }

        // Listen to session changes for real-time isRevisionMode updates
        viewModelScope.launch {
            repository.getCircleSessions(session.circleId).collect { result ->
                result.onSuccess { sessions ->
                    val updatedSession = sessions.find { it.id == session.id }
                    if (updatedSession != null) {
                        currentSession = updatedSession
                        isRevisionPhase = updatedSession.isRevisionMode
                    }
                }
            }
        }

        // Listen to payment status changes for current user
        viewModelScope.launch {
            Log.d("TitipankuViewModel", "Starting payment listener for session=${session.id}, user=$currentUserId")
            repository.listenToPaymentsBySessionAndUser(session.id, currentUserId).collect { result ->
                result.onSuccess { payments ->
                    Log.d("TitipankuViewModel", "Received ${payments.size} payments")
                    // Get latest payment for this user
                    val myPayment = payments.firstOrNull()
                    myPaymentStatus = myPayment?.status ?: "pending"
                    Log.d("TitipankuViewModel", "Updated myPaymentStatus to: $myPaymentStatus")
                }.onFailure { error ->
                    Log.e("TitipankuViewModel", "Payment listener error", error)
                }
            }
        }

        // Listen to ALL payments for this session (for disbursement validation)
        viewModelScope.launch {
            paymentRepository.getSessionPayments(session.id).collect { result ->
                result.onSuccess { payments ->
                    sessionPayments.clear()
                    sessionPayments.addAll(payments)
                    Log.d("TitipankuViewModel", "Session has ${payments.size} total payments")
                }
            }
        }
    }

    private fun startTimer(session: JastipSession) {
        timerJob?.cancel()

        val durationMillis = session.durationMinutes * 60 * 1000L
        val endTime = session.createdAt.toDate().time + durationMillis

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

    fun toggleRevisionMode() {
        val session = currentSession ?: return
        val newRevisionMode = !session.isRevisionMode

        viewModelScope.launch {
            repository.toggleRevisionMode(session.id, newRevisionMode).collect { result ->
                result.onSuccess {
                    isRevisionPhase = newRevisionMode
                    // Send chat notification
                    val msg = if (newRevisionMode) {
                        "🔄 MODE REVISI DIAKTIFKAN! Jastiper sedang mengecek ketersediaan barang."
                    } else {
                        "✅ Mode revisi selesai. Belanja dilanjutkan."
                    }
                    sendChatSystem(session.id, msg)
                }
            }
        }
    }

    fun clearMessage() { uiMessage = null }

    // ===== DISBURSEMENT FUNCTIONS =====

    fun requestDisbursement() {
        val session = currentSession ?: return

        // Validation
        if (session.creatorId != currentUserId) {
            disbursementMessage = "Hanya Jastiper yang bisa cairkan dana"
            return
        }

        if (session.status != "closed") {
            disbursementMessage = "Session harus ditutup dulu sebelum cairkan dana"
            return
        }

        if (!canDisburse) {
            disbursementMessage = "Tunggu semua user bayar dulu sebelum cairkan dana"
            return
        }

        // Calculate total amount
        val totalAmount = sessionPayments
            .filter { it.status == "success" }
            .sumOf { it.amount }

        if (totalAmount <= 0.0) {
            disbursementMessage = "Tidak ada pembayaran yang berhasil"
            return
        }

        // Request disbursement
        disbursementStatus = "loading"
        disbursementMessage = null

        viewModelScope.launch {
            paymentRepository.disburseFunds(
                sessionId = session.id,
                jastiperId = currentUserId
            ).collect { result ->
                result.onSuccess { response ->
                    disbursementStatus = "success"
                    disbursementAmount = response.netAmount
                    disbursementMessage = "Dana berhasil dicairkan: Rp ${response.netAmount.toInt()}"
                    Log.d("TitipankuViewModel", "Disbursement success: ${response.netAmount}")
                }.onFailure { error ->
                    disbursementStatus = "failed"
                    disbursementMessage = when {
                        error.message?.contains("bank account", ignoreCase = true) == true ->
                            "Gagal: Daftar rekening bank dulu di Profile"
                        error.message?.contains("payments", ignoreCase = true) == true ->
                            "Gagal: Belum ada pembayaran yang berhasil"
                        else ->
                            "Gagal cairkan dana: ${error.message}"
                    }
                    Log.e("TitipankuViewModel", "Disbursement failed", error)
                }
            }
        }
    }

    fun clearDisbursementMessage() {
        disbursementMessage = null
    }

    fun retryDisbursement() {
        disbursementStatus = null
        disbursementMessage = null
        requestDisbursement()
    }

}