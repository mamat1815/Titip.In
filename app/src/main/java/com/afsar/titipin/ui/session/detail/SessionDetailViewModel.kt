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
import com.afsar.titipin.data.model.DisbursementInfo
import com.afsar.titipin.data.model.Order
import com.afsar.titipin.data.model.OrderItem
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

    // Ambil ID dari Navigasi
    val sessionId: String = checkNotNull(savedStateHandle["sessionId"])

    // --- STATE DATA ---
    var sessionState by mutableStateOf<Session?>(null)
    var orders by mutableStateOf<List<Order>>(emptyList())

    // User Info
    var currentUserId by mutableStateOf("")
    var currentUser by mutableStateOf<User?>(null)

    // Chat & Payment Data
    var chatMessages = mutableStateListOf<ChatMessage>()
    private var sessionPayments = mutableStateListOf<PaymentInfo>()

    // --- FORM INPUT ---
    var chatInput by mutableStateOf("")
    var orderItemName by mutableStateOf("")
    var orderQuantity by mutableIntStateOf(1)
    var orderPriceEstimate by mutableStateOf("")
    var orderNotes by mutableStateOf("")
    var orderTip by mutableStateOf("")

    // --- STATUS & TIMER ---
    var timeString by mutableStateOf("Loading...")
    var isRevisionPhase by mutableStateOf(false)
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var uiMessage by mutableStateOf<String?>(null)

    // --- DISBURSEMENT ---
    var disbursementStatus by mutableStateOf<String?>(null)
    var disbursementMessage by mutableStateOf<String?>(null)
    var disbursementAmount by mutableDoubleStateOf(0.0)
    var existingDisbursement by mutableStateOf<DisbursementInfo?>(null)
    private var timerJob: Job? = null

    companion object {
        private const val PAYMENT_FEE_PERCENTAGE = 0.02
        private const val PAYMENT_FEE_FIXED = 2500.0
        private const val DISBURSEMENT_FEE = 5000.0
    }

    init {
        loadData()
    }

    // ==========================================
    // --- LOAD DATA ---
    // ==========================================
    private fun loadData() {
        isLoading = true

        // 1. Load Profile
        viewModelScope.launch {
            authRepository.getUserProfile().collect { result ->
                result.onSuccess { user ->
                    currentUser = user
                    currentUserId = user.uid
                }
            }
        }

        // 2. Load Session Detail (Realtime)
        viewModelScope.launch {
            sessionRepository.getSessionById(sessionId).collect { result ->
                result.onSuccess { session ->
                    sessionState = session
                    isRevisionPhase = session.isRevisionMode
                    startTimer(session)
                    loadOrdersAndChat(session.id)
                    listenToPayments(session.id)
                    checkDisbursementStatus(session.id)
                }.onFailure {
                    errorMessage = "Gagal memuat sesi: ${it.message}"
                }
                isLoading = false
            }
        }
    }

    private fun checkDisbursementStatus(sessionId: String) {
        viewModelScope.launch {
            paymentRepository.getDisbursementBySession(sessionId).collect { result ->
                result.onSuccess { data ->
                    existingDisbursement = data

                    // Jika sudah ada data pencairan, update status UI
                    if (data != null) {
                        disbursementStatus = if(data.status == "completed") "success" else data.status
                        disbursementMessage = "Dana sudah dicairkan pada: ${data.requestedAt}"
                    }

                    // Hitung ulang validasi tombol
                    calculateTotals()
                }
            }
        }
    }

    private fun loadOrdersAndChat(sessionId: String) {
        // Load Orders
        viewModelScope.launch {
            orderRepository.getOrdersBySession(sessionId).collect { result ->
                result.onSuccess { list ->
                    orders = list
                    // Hitung total setiap kali data order baru masuk
                    calculateTotals()
                }.onFailure {
                    Log.e("SessionVM", "Error load orders: ${it.message}")
                }
            }
        }

        // Load Chat
        viewModelScope.launch {
            sessionRepository.getSessionChatMessages(sessionId).collect { msgs ->
                chatMessages.clear()
                chatMessages.addAll(msgs)
            }
        }
    }

    // ==========================================
    // --- FINANCIAL CALCULATIONS ---
    // ==========================================

    // -- Variable Helper untuk Perhitungan Realtime --
    // Kita pindahkan state perhitungan ke variable mutableState agar bisa diupdate manual jika perlu

    // Status Pembayaran Saya
    val myPaymentStatus by derivedStateOf {
        val myPayment = sessionPayments.find { it.userId == currentUserId }
        myPayment?.status ?: "pending"
    }

    // HITUNGAN GUEST
    var myTotalGoodsPrice by mutableDoubleStateOf(0.0)
    var myTotalJastipFee by mutableDoubleStateOf(0.0)

    val mySubTotal by derivedStateOf { myTotalGoodsPrice + myTotalJastipFee }

    val myAdminFee by derivedStateOf {
        if (mySubTotal > 0) {
            val percentageFee = mySubTotal * PAYMENT_FEE_PERCENTAGE
            ceil(percentageFee + PAYMENT_FEE_FIXED)
        } else { 0.0 }
    }

    val myGrandTotal by derivedStateOf { mySubTotal + myAdminFee }

    val canIPay by derivedStateOf { myGrandTotal > 0.0 && myPaymentStatus != "success" }

    // HITUNGAN HOST
    var totalCollectedGoodsPrice by mutableDoubleStateOf(0.0)
    var netDisbursementAmount by mutableDoubleStateOf(0.0)
    var canDisburse by mutableStateOf(false)

    // Fungsi Sentral untuk Menghitung Ulang Semua Angka Keuangan
    private fun calculateTotals() {
        // 1. Hitungan Guest (Saya)
        val myValidOrders = orders.filter {
            it.requesterId == currentUserId && (it.status == "accepted" || it.status == "bought")
        }
        myTotalGoodsPrice = myValidOrders.sumOf { order ->
            order.items.sumOf { it.priceEstimate * it.quantity }
        }
        myTotalJastipFee = myValidOrders.sumOf { it.jastipFee }

        // 2. Hitungan Host (Pencairan)
        val paidUserIds = sessionPayments
            .filter { it.status == "success" }
            .map { it.userId }

        val paidOrders = orders.filter { it.requesterId in paidUserIds }

        // Total yang bisa dicairkan = (Harga Barang + Jastip Fee) dari user yang LUNAS
        val totalCollected = paidOrders.sumOf { order ->
            val itemsTotal = order.items.sumOf { it.priceEstimate * it.quantity }
            itemsTotal + order.jastipFee
        }

        totalCollectedGoodsPrice = totalCollected
        netDisbursementAmount = if (totalCollected > 0) maxOf(0.0, totalCollected - DISBURSEMENT_FEE) else 0.0

        val session = sessionState
        val isCreator = session?.creatorId == currentUserId
        val isClosed = session?.status == "closed"
//        canDisburse = isCreator && isClosed && netDisbursementAmount > 0.0
        canDisburse = isCreator && isClosed && netDisbursementAmount > 0.0 && existingDisbursement == null
    }


    // ==========================================
    // --- ACTIONS: UPDATE PRICE & ITEMS ---
    // ==========================================

    fun updateItemPrice(orderId: String, itemIndex: Int, newPrice: Double) {
        viewModelScope.launch {
            val targetOrder = orders.find { it.id == orderId } ?: return@launch

            // Edit item di memory
            val updatedItems = targetOrder.items.toMutableList()
            val oldItem = updatedItems[itemIndex]
            updatedItems[itemIndex] = oldItem.copy(priceEstimate = newPrice)

            // Kirim ke Firestore (Pakai fungsi baru di Repo)
            orderRepository.updateOrderItems(orderId, updatedItems).collect { result ->
                result.onSuccess {
                    // Update Local List & Recalculate
                    updateLocalOrderList(orderId, updatedItems)
                    uiMessage = "Harga berhasil diubah menjadi Rp ${newPrice.toInt()}"
                }
                result.onFailure {
                    uiMessage = "Gagal update harga: ${it.localizedMessage}"
                }
            }
        }
    }

    // Toggle Checklist Item (Di Shopping List)
    fun toggleItemStatus(order: Order, itemIndex: Int) {
        val currentItem = order.items[itemIndex]
        val newStatus = if (currentItem.status == "bought") "pending" else "bought"
        updateItemStatusInOrder(order, itemIndex, newStatus)
    }

    // Tandai Item Revisi
    fun flagItemRevision(order: Order, itemIndex: Int) {
        updateItemStatusInOrder(order, itemIndex, "revision")
    }

    // Helper Update Item Status
    private fun updateItemStatusInOrder(order: Order, itemIndex: Int, newStatus: String) {
        val updatedItems = order.items.toMutableList()
        updatedItems[itemIndex] = updatedItems[itemIndex].copy(status = newStatus)

        // Cek apakah semua item sudah dibeli? Jika ya, update status Order Induk
        val allBought = updatedItems.all { it.status == "bought" }
        val newOrderStatus = if (allBought) "bought" else "accepted"

        viewModelScope.launch {
            // 1. Update Items dulu (status per barang)
            orderRepository.updateOrderItems(order.id, updatedItems).collect { res ->
                res.onSuccess {
                    // 2. Jika sukses, update juga status Order Induk (jika berubah)
                    if (order.status != newOrderStatus) {
                        orderRepository.updateOrderStatus(order.circleId, order.sessionId, order.id, newOrderStatus).collect{}
                    }

                    // 3. Update UI Local
                    // Kita update status order induk juga di memory biar UI checklist hijau
                    val updatedOrder = order.copy(items = updatedItems, status = newOrderStatus)

                    val index = orders.indexOfFirst { it.id == order.id }
                    if (index != -1) {
                        val newList = orders.toMutableList()
                        newList[index] = updatedOrder
                        orders = newList
                        calculateTotals()
                    }
                }
            }
        }
    }

    // Helper untuk update list local agar UI responsif tanpa fetch ulang
    private fun updateLocalOrderList(orderId: String, newItems: List<OrderItem>) {
        val index = orders.indexOfFirst { it.id == orderId }
        if (index != -1) {
            val oldOrder = orders[index]
            val updatedOrder = oldOrder.copy(items = newItems)

            // Hitung ulang total estimate di order object (untuk konsistensi)
            val newTotal = newItems.sumOf { it.priceEstimate * it.quantity }
            val finalOrder = updatedOrder.copy(totalEstimate = newTotal)

            val newList = orders.toMutableList()
            newList[index] = finalOrder
            orders = newList

            calculateTotals() // Hitung ulang duit
        }
    }

    // ==========================================
    // --- ACTIONS: CREATE ORDER ---
    // ==========================================
    fun createOrder(onSuccess: () -> Unit) {
        val session = sessionState ?: return
        val user = currentUser

        if (orderItemName.isBlank()) { uiMessage = "Nama barang wajib diisi"; return }
        if (orderPriceEstimate.isBlank()) { uiMessage = "Estimasi harga wajib diisi"; return }

        val cleanPrice = orderPriceEstimate.replace(Regex("[^0-9]"), "").toDoubleOrNull() ?: 0.0
        val cleanTip = orderTip.replace(Regex("[^0-9]"), "").toDoubleOrNull() ?: 0.0

        if (cleanPrice <= 0.0) { uiMessage = "Harga tidak valid"; return }

        isLoading = true

        val newItem = OrderItem(
            name = orderItemName,
            quantity = orderQuantity,
            priceEstimate = cleanPrice,
            notes = orderNotes
        )
        val totalCalculated = cleanPrice * orderQuantity

        val newOrder = Order(
            sessionId = session.id,
            circleId = session.circleId,
            items = listOf(newItem),
            totalEstimate = totalCalculated,
            jastipFee = cleanTip,
            requesterId = user!!.uid,
            requesterName = user.name,
            requesterPhotoUrl = user.photoUrl,
            status = "pending"
        )

        viewModelScope.launch {
            orderRepository.createOrder(session.circleId, session.id, newOrder).collect { res ->
                isLoading = false
                res.onSuccess {
                    orderItemName = ""; orderPriceEstimate = ""; orderQuantity = 1; orderNotes = ""; orderTip = ""
                    onSuccess()
                }.onFailure {
                    uiMessage = "Gagal kirim order: ${it.message}"
                }
            }
        }
    }

    // Update Status Order Induk (Terima/Tolak di Awal)
    fun updateOrderStatus(orderId: String, status: String) {
        val session = sessionState ?: return
        viewModelScope.launch {
            orderRepository.updateOrderStatus(session.circleId, session.id, orderId, status).collect {}
        }
    }

    // ==========================================
    // --- ACTIONS: SESSION FLOW ---
    // ==========================================
    fun startShopping(onSuccess: () -> Unit) {
        val session = sessionState ?: return
        isLoading = true
        val shoppingSession = session.copy(status = "shopping")
        viewModelScope.launch {
            sessionRepository.updateSession(session.circleId, session.id, shoppingSession).collect {
                isLoading = false; onSuccess()
            }
        }
    }

    fun finishSession() {
        val session = sessionState ?: return
        viewModelScope.launch {
            val closedSession = session.copy(status = "closed")
            sessionRepository.updateSession(session.circleId, session.id, closedSession).collect {}
        }
    }

    // ==========================================
    // --- ACTIONS: DISBURSEMENT ---
    // ==========================================
    fun requestDisbursement() {
        val session = sessionState ?: return
        val bank = currentUser?.bank
        if (bank == null || bank.bankAccountNumber.isEmpty()) {
            disbursementMessage = "Rekening belum diatur. Cek Profil."; disbursementStatus = "failed"
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
                result.onSuccess { response ->
                    disbursementStatus = "success"
                    disbursementMessage = "Dana cair: Rp ${response.netAmount.toLong()}"
                    disbursementAmount = response.netAmount
                }.onFailure {
                    disbursementStatus = "failed"
                    disbursementMessage = "Gagal cair: ${it.message}"
                }
            }
        }
    }

    fun retryDisbursement() { requestDisbursement() }

    // Chat
    fun sendChat() {
        val session = sessionState ?: return
        if (chatInput.isNotBlank()) {
            val msg = chatInput; chatInput = ""
            viewModelScope.launch {
                sessionRepository.sendSessionChatMessage(session.circleId, sessionId, msg).collect {}
            }
        }
    }

    private fun listenToPayments(sessionId: String) {
        viewModelScope.launch {
            paymentRepository.getSessionPayments(sessionId).collect { result ->
                result.onSuccess { list ->
                    sessionPayments.clear()
                    sessionPayments.addAll(list)
                    calculateTotals() // Update disbursement info if new payments arrive
                }
            }
        }
    }

    private fun startTimer(session: Session) {
        timerJob?.cancel()
        val createdAt = session.createdAt ?: return
        timerJob = viewModelScope.launch {
            while (true) {
                if (session.status != "open") { timeString = "Selesai"; break }
                val end = createdAt.toDate().time + (session.durationMinutes * 60 * 1000L)
                val left = end - System.currentTimeMillis()
                if (left > 0) {
                    timeString = String.format("%02d:%02d", (left / 1000) / 60, (left / 1000) % 60)
                } else {
                    timeString = "Waktu Habis"; break
                }
                delay(1000)
            }
        }
    }

    // ==========================================
    // --- UI HELPERS ---
    // ==========================================
    val totalItems by derivedStateOf {
        orders.filter { it.status == "accepted" || it.status == "bought" }
            .flatMap { it.items }.sumOf { it.quantity }
    }

    val totalPrice by derivedStateOf {
        orders.filter { it.status == "accepted" || it.status == "bought" }
            .sumOf { it.totalEstimate }
    }

    // Host Tab Logic
    var selectedTabIndex by mutableIntStateOf(0)
    val pendingCount by derivedStateOf { orders.count { it.status == "pending" } }
    val acceptedCount by derivedStateOf { orders.count { it.status == "accepted" || it.status == "bought" } }
    val rejectedCount by derivedStateOf { orders.count { it.status == "rejected" } }

    val displayedOrdersForHost by derivedStateOf {
        when (selectedTabIndex) {
            0 -> orders.filter { it.status == "pending" }
            1 -> orders.filter { it.status == "accepted" || it.status == "bought" }
            2 -> orders.filter { it.status == "rejected" }
            else -> emptyList()
        }
    }

    val isReadyToShop by derivedStateOf { acceptedCount > 0 }

    fun clearMessage() { uiMessage = null }
    fun clearDisbursementMessage() { disbursementMessage = null }
}