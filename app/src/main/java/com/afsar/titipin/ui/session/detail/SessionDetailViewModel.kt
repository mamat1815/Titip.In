package com.afsar.titipin.ui.session.detail

import android.util.Log
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
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
class SessionDetailViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val orderRepository: OrderRepository,
    private val authRepository: AuthRepository,
    private val paymentRepository: PaymentRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val sessionId: String = checkNotNull(savedStateHandle["sessionId"])

    // --- State Data ---
    var sessionState by mutableStateOf<Session?>(null)
    var orders by mutableStateOf<List<Order>>(emptyList())
    var currentUserId by mutableStateOf("")
    var currentUser by mutableStateOf<User?>(null)
    var chatMessages = mutableStateListOf<ChatMessage>()

    // List pembayaran (Moved up for clarity)
    private var sessionPayments = mutableStateListOf<PaymentInfo>()

    // --- UI Inputs ---
    var chatInput by mutableStateOf("")
    var orderItemName by mutableStateOf("")
    var orderQuantity by mutableIntStateOf(1)
    var orderPriceEstimate by mutableStateOf("")
    var orderNotes by mutableStateOf("")

    // --- Status ---
    var timeString by mutableStateOf("Loading...")
    var isRevisionPhase by mutableStateOf(false)
    private var timerJob: Job? = null
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var uiMessage by mutableStateOf<String?>(null)

    // --- Disbursement State ---
    var disbursementStatus by mutableStateOf<String?>(null)
    var disbursementMessage by mutableStateOf<String?>(null)
    var disbursementAmount by mutableDoubleStateOf(0.0)

    companion object {
        // Biaya Gateway (Midtrans) estimasi: 2.9% + Rp 2.000 (Dibebankan ke Penitip)
        private const val PAYMENT_FEE_PERCENTAGE = 0.029
        private const val PAYMENT_FEE_FIXED = 2000.0

        // Biaya Transfer Bank (Iris/Flip) saat Jastiper cairkan dana (Dibebankan ke Jastiper)
        private const val DISBURSEMENT_FEE = 5000.0
    }

    init {
        loadData()
    }

    private fun loadData() {
        isLoading = true

        // 1. Load User Profile
        viewModelScope.launch {
            authRepository.getUserProfile().collect { res ->
                res.fold(
                    onSuccess = { user ->
                        currentUser = user
                        currentUserId = user.uid // Penting: update ID agar sinkron
                    },
                    onFailure = { /* Handle error */ }
                )
            }
        }

        // 2. Load Session Detail
        viewModelScope.launch {
            sessionRepository.getSessionById(sessionId).collect { result ->
                result.fold(
                    onSuccess = { session ->
                        sessionState = session
                        isRevisionPhase = session.isRevisionMode
                        startTimer(session)
                        listenToPayments(session.id)
                    },
                    onFailure = {
                        errorMessage = "Gagal memuat sesi: ${it.message}"
                    }
                )
                isLoading = false
            }
        }

        // 3. Load Orders
        viewModelScope.launch {
            orderRepository.getOrdersBySession(sessionId).collect { res ->
                res.fold(
                    onSuccess = { orders = it },
                    onFailure = { Log.d("SessionVM","Gagal memuat pesanan: ${it.message}")}
                )
            }
        }

        // 4. Load Chat
        viewModelScope.launch {
            sessionRepository.getSessionChatMessages(sessionId).collect { messages ->
                chatMessages.clear()
                chatMessages.addAll(messages)
            }
        }
    }

    // ==========================================
    // --- CALCULATIONS (LOGIKA HITUNGAN) ---
    // ==========================================

    // 1. Status Pembayaran User Saat Ini
    val myPaymentStatus by derivedStateOf {
        val myPayment = sessionPayments.find { it.userId == currentUserId }
        myPayment?.status ?: "pending"
    }

    // ------------------------------------------
    // A. LOGIKA UNTUK PENITIP (Tagihan Saya)
    // ------------------------------------------

    val myTotalGoodsPrice by derivedStateOf {
        // --- MULAI DEBUG ---
        val myId = currentUserId

        // 1. Cek User ID
        Log.d("TAGIHAN_CEK", "=== MULAI HITUNG ===")
        Log.d("TAGIHAN_CEK", "Login sebagai ID: '$myId'")

        // 2. Cek Order Mentah
        Log.d("TAGIHAN_CEK", "Total Semua Order di Sesi: ${orders.size}")
        orders.forEach {
            Log.d("TAGIHAN_CEK", " - Order Item: ${it.itemName} | Requester: ${it.requesterId} | Status: ${it.status} | Harga: ${it.priceEstimate}")
        }

        // 3. Cek Filter Punya Saya
        val myOrders = orders.filter { it.requesterId == myId }
        Log.d("TAGIHAN_CEK", "Order Punya Saya: ${myOrders.size}")

        // 4. Cek Filter Status (Accepted/Bought)
        val validOrders = myOrders.filter { it.status == "accepted" || it.status == "bought" }
        Log.d("TAGIHAN_CEK", "Order Siap Bayar (Valid): ${validOrders.size}")

        // 5. Hitung Total
        val total = validOrders.sumOf { it.priceEstimate * it.quantity }
        Log.d("TAGIHAN_CEK", "HASIL HITUNGAN: $total")
        Log.d("TAGIHAN_CEK", "====================")
        // --- SELESAI DEBUG ---

        total
    }

    // Biaya Admin Aplikasi (Midtrans Fee + Keuntungan App)
    // Rumus: (Harga Barang * 2.9%) + 2.000
    val myAdminFee by derivedStateOf {
        if (myTotalGoodsPrice > 0) {
            ceil((myTotalGoodsPrice * PAYMENT_FEE_PERCENTAGE) + PAYMENT_FEE_FIXED)
        } else {
            0.0
        }
    }

    // Total yang harus ditransfer Penitip (Grand Total)
    val myGrandTotal by derivedStateOf {
        myTotalGoodsPrice + myAdminFee
    }

    // Validasi tombol bayar (Aktif jika ada tagihan dan belum lunas)
    val canIPay by derivedStateOf {
        myTotalGoodsPrice > 0.0 && myPaymentStatus != "success"
    }

    // ------------------------------------------
    // B. LOGIKA UNTUK JASTIPER (Pencairan Dana)
    // ------------------------------------------

    // Total Harga Barang yang SUDAH DIBAYAR LUNAS oleh para penitip
    // (Jastiper hanya berhak atas harga barang, bukan biaya admin)
    val totalCollectedGoodsPrice by derivedStateOf {
        // Cari siapa saja yang sudah bayar lunas
        val paidUserIds = sessionPayments
            .filter { it.status == "success" }
            .map { it.userId }

        // Jumlahkan harga barang dari order milik user yang sudah bayar
        orders
            .filter { it.requesterId in paidUserIds }
            .sumOf { it.priceEstimate * it.quantity }
    }

    // Uang Bersih yang Diterima Jastiper
    // Rumus: Total Barang Lunas - Biaya Transfer Bank (Rp 5.000)
    val netDisbursementAmount by derivedStateOf {
        if (totalCollectedGoodsPrice > 0) {
            maxOf(0.0, totalCollectedGoodsPrice - DISBURSEMENT_FEE)
        } else {
            0.0
        }
    }

    // Validasi Tombol Cairkan
    val canDisburse by derivedStateOf {
        val session = sessionState ?: return@derivedStateOf false
        val isCreator = session.creatorId == currentUserId
        val isClosed = session.status == "closed"

        // Bisa cair jika user adalah creator, sesi tutup, dan ada saldo bersih
        isCreator && isClosed && netDisbursementAmount > 0.0
    }

    // ------------------------------------------
    // C. LOGIKA HELPER UI LAINNYA
    // ------------------------------------------

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

    // Is Payment Required? (Logic lama, dipertahankan untuk backward compatibility UI)
    val isPaymentRequired by derivedStateOf {
        // Logic baru: Tampilkan tombol bayar jika ada tagihan yang sah
        myGrandTotal > 0.0
    }

    // ==========================================
    // --- ACTIONS ---
    // ==========================================

    fun sendChat() {
        val session = sessionState ?: return
        if (chatInput.isNotBlank()) {
            val msg = chatInput; chatInput = ""
            viewModelScope.launch {
                sessionRepository.sendSessionChatMessage(session.circleId, sessionId, msg).collect {}
            }
        }
    }

    fun toggleItemBought(order: Order) {
        val newStatus = if (order.status == "bought") "accepted" else "bought"
        updateOrderStatus(order.id, newStatus)
    }

    fun flagItemForRevision(order: Order) {
        updateOrderStatus(order.id, "revision")
        sendSystemChat("⚠️ Stok untuk '${order.itemName}' kosong/bermasalah.")
    }

    // Hapus private agar bisa dipanggil UI
    fun updateOrderStatus(orderId: String, status: String) {
        val session = sessionState ?: return
        viewModelScope.launch {
            orderRepository.updateOrderStatus(session.circleId, session.id, orderId, status).collect {}
        }
    }

    fun createOrder(onSuccess: () -> Unit) {
        val session = sessionState ?: return
        val user = currentUser

        if (orderItemName.isBlank()) {
            uiMessage = "Nama barang wajib diisi"
            return
        }
        if (user == null) {
            uiMessage = "Data profil belum termuat, tunggu sebentar..."
            return
        }

        isLoading = true

        // Ambil harga dari input, default 0.0 jika error/kosong
        val price = orderPriceEstimate.toDoubleOrNull() ?: 0.0

        val newOrder = Order(
            sessionId = session.id,
            circleId = session.circleId,
            itemName = orderItemName,
            quantity = orderQuantity,
            priceEstimate = price,
            totalPrice = price * orderQuantity, // Hitung total per item
            notes = orderNotes,
            status = "pending",

            // WAJIB: Masukkan data User agar tidak null di database
            requesterId = user.uid,
            requesterName = user.name,
            requesterPhotoUrl = user.photoUrl
        )

        viewModelScope.launch {
            orderRepository.createOrder(session.circleId, session.id, newOrder).collect { res ->
                isLoading = false
                res.fold(
                    onSuccess = {
                        orderItemName = ""
                        orderQuantity = 1
                        orderPriceEstimate = ""
                        orderNotes = ""
                        onSuccess()
                    },
                    onFailure = {
                        uiMessage = "Gagal membuat pesanan: ${it.message}"
                    }
                )
            }
        }
    }

    fun finishSession() {
        val session = sessionState ?: return
        viewModelScope.launch {
            val closed = session.copy(status = "closed")
            sessionRepository.updateSession(session.circleId, session.id, closed).collect {}
        }
    }

    // --- DISBURSEMENT ---
    fun requestDisbursement() {
        val session = sessionState ?: return
        val bank = currentUser?.bank

        if (bank == null || bank.bankAccountNumber.isEmpty()) {
            disbursementMessage = "Rekening belum diatur di Profil."; disbursementStatus = "failed"
            return
        }

        disbursementStatus = "loading"; disbursementMessage = null
        viewModelScope.launch {
            paymentRepository.disburseFunds(
                sessionId = session.id,
                jastiperId = currentUserId,
                bankCode = bank.bankCode,
                accountNumber = bank.bankAccountNumber,
                accountName = bank.bankAccountName
            ).collect { result ->
                result.fold(
                    onSuccess = { response ->
                        disbursementStatus = "success"
                        disbursementMessage = "Sukses cairkan dana!"
                        disbursementAmount = response.netAmount
                    },
                    onFailure = { error ->
                        disbursementStatus = "failed"
                        disbursementMessage = error.message
                    }
                )
            }
        }
    }

    fun retryDisbursement() = requestDisbursement()
    fun clearDisbursementMessage() { disbursementMessage = null }
    fun clearMessage() { uiMessage = null }

    private fun sendSystemChat(msg: String) {
        val session = sessionState ?: return
        viewModelScope.launch {
            sessionRepository.sendSessionChatMessage(session.circleId, sessionId, msg).collect {}
        }
    }

    private fun startTimer(session: Session) {
        timerJob?.cancel()
        val createdAt = session.createdAt ?: return

        timerJob = viewModelScope.launch {
            while (true) {
                if (session.status != "open") {
                    timeString = "Selesai"; break
                }

                val end = createdAt.toDate().time + (session.durationMinutes * 60 * 1000L)
                val left = end - System.currentTimeMillis()

                if (left > 0) {
                    timeString = String.format("%02d:%02d", (left / 1000) / 60, (left / 1000) % 60)
                } else {
                    timeString = "Waktu Habis";
                    break
                }
                delay(1000)
            }
        }
    }

    private fun listenToPayments(sessionId: String) {
        viewModelScope.launch {
            paymentRepository.getSessionPayments(sessionId).collect { res ->
                res.fold(
                    onSuccess = { list ->
                        sessionPayments.clear()
                        sessionPayments.addAll(list)
                    },
                    onFailure = {}
                )
            }
        }
    }
}