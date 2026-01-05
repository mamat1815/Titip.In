package com.afsar.titipin.ui.payment

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afsar.titipin.data.model.Order
import com.afsar.titipin.data.remote.AuthRepository
import com.afsar.titipin.data.remote.PaymentRepository
import com.afsar.titipin.data.remote.repository.order.OrderRepository
import com.afsar.titipin.data.remote.repository.session.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.ceil

@HiltViewModel
class PaymentDetailViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val authRepository: AuthRepository,
    private val sessionRepository: SessionRepository,
    private val paymentRepository: PaymentRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val sessionId: String = checkNotNull(savedStateHandle["sessionId"])

    var isLoading by mutableStateOf(true)
    var orders by mutableStateOf<List<Order>>(emptyList())
    var errorMessage by mutableStateOf<String?>(null)

    // User Info
    var currentUserName by mutableStateOf("")
    var currentUserEmail by mutableStateOf("")
    var currentUserPhone by mutableStateOf("")
    var currentUserId by mutableStateOf("")

    // --- ROLE STATE ---
    var isCreator by mutableStateOf(false)
    var sessionStatus by mutableStateOf("open")
    var myPaymentStatus by mutableStateOf("none")

    // --- DISBURSEMENT STATE ---
    var disbursementStatus by mutableStateOf<String?>(null)
    var disbursementMessage by mutableStateOf<String?>(null)

    // Hitungan
    val myTotalGoodsPrice: Double get() = orders.sumOf { it.totalEstimate }
    val myJastipFee: Double get() = orders.sumOf { it.jastipFee }
    val mySubTotal: Double get() = myTotalGoodsPrice + myJastipFee
    val myAdminFee: Double get() = if (mySubTotal > 0) ceil((mySubTotal * 0.02) + 2500) else 0.0
    val myGrandTotal: Double get() = mySubTotal + myAdminFee

    // Hitungan Host
    val totalCollected: Double get() = orders.sumOf { it.totalEstimate + it.jastipFee }
    val disbursementFee: Double = 5000.0
    val netDisbursement: Double get() = if (totalCollected > 0) totalCollected - disbursementFee else 0.0

    // PERBAIKAN LOGIC: Hanya bisa cairkan jika saldo > 0 DAN belum pernah sukses cair
    val canDisburse: Boolean get() = netDisbursement > 0 && disbursementStatus != "success" && disbursementStatus != "completed"

    val isPaid: Boolean
        get() = myPaymentStatus == "success" || myPaymentStatus == "settlement" || myPaymentStatus == "capture"

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser()
            if (user == null) {
                errorMessage = "User tidak ditemukan"
                isLoading = false
                return@launch
            }

            currentUserId = user.uid
            currentUserName = user.displayName ?: "User"
            currentUserEmail = user.email ?: ""
            currentUserPhone = user.phoneNumber ?: "08123456789"

            checkPaymentStatus(sessionId, user.uid)

            // 1. Cek Detail Sesi
            sessionRepository.getSessionById(sessionId).collect { resSession ->
                resSession.onSuccess { session ->
                    isCreator = session.creatorId == currentUserId
                    sessionStatus = session.status

                    // 2. Jika saya Creator, CEK STATUS PENCAIRAN SEKARANG
                    if (isCreator) {
                        checkIfAlreadyDisbursed()
                    }

                    // 3. Ambil Order
                    fetchOrders()
                }
            }
        }
    }

    private fun fetchOrders() {
        viewModelScope.launch {
            orderRepository.getOrdersBySession(sessionId).collect { result ->
                result.onSuccess { allOrders ->
                    if (isCreator) {
                        orders = allOrders.filter { it.status == "accepted" || it.status == "bought" }
                    } else {
                        orders = allOrders.filter {
                            it.requesterId == currentUserId && (it.status == "accepted" || it.status == "bought")
                        }
                    }
                    isLoading = false
                }.onFailure {
                    errorMessage = "Gagal memuat data: ${it.message}"
                    isLoading = false
                }
            }
        }
    }

    private fun checkPaymentStatus(sessionId: String, userId: String) {
        viewModelScope.launch {
            paymentRepository.getUserPaymentStatus(sessionId, userId).collect { result ->
                result.onSuccess { status ->
                    myPaymentStatus = status
                }
            }
        }
    }

    // --- FUNGSI CEK STATUS PENCAIRAN (FRONTEND SECURITY) ---
    private fun checkIfAlreadyDisbursed() {
        viewModelScope.launch {
            // Memanggil fungsi repo yang kamu buat (getDisbursementBySession)
            paymentRepository.getDisbursementBySession(sessionId).collect { result ->
                result.onSuccess { data ->
                    if (data != null) {
                        // DATA DITEMUKAN! Berarti sudah pernah cair.
                        // Ubah status jadi success/completed agar tombol mati.
                        disbursementStatus = if (data.status == "completed") "success" else data.status

                        // Format tanggal jika ada
                        val dateStr = data.requestedAt?.toDate()?.toString() ?: "Baru saja"
                        disbursementMessage = "Dana sudah dicairkan pada: $dateStr"
                    }
                }
            }
        }
    }

    // --- FUNGSI REQUEST PENCAIRAN ---
    fun requestDisbursement() {
        // Double Check: Jangan jalan kalau sudah sukses/loading
        if (!isCreator || !canDisburse || disbursementStatus == "loading" || disbursementStatus == "success") return

        disbursementStatus = "loading"
        viewModelScope.launch {
            paymentRepository.disburseFunds(
                sessionId = sessionId,
                jastiperId = currentUserId,
                bankCode = "bca",
                accountNumber = "1234567890",
                accountName = currentUserName
            ).collect { result ->
                result.onSuccess {
                    disbursementStatus = "success"
                    disbursementMessage = "Dana berhasil dicairkan! Cek rekeningmu."
                    // Setelah sukses, checkIfAlreadyDisbursed akan otomatis update lagi karena realtime listener
                }.onFailure { e ->
                    disbursementStatus = "failed"
                    disbursementMessage = "Gagal: ${e.message}"
                }
            }
        }
    }

    fun clearDisbursementMessage() {
        disbursementMessage = null
    }
}