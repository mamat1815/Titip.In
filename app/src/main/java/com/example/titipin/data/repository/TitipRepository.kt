package com.example.titipin.data.repository

import com.example.titipin.R
import com.example.titipin.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TitipRepository {
    private val _titipSessions = MutableStateFlow(getDummyTitipSessions())
    val titipSessions: StateFlow<List<TitipSession>> = _titipSessions.asStateFlow()
    
    private val _dititipiSessions = MutableStateFlow(getDummyDititipiSessions())
    val dititipiSessions: StateFlow<List<DititipiSession>> = _dititipiSessions.asStateFlow()
    
    private fun getDummyTitipSessions() = listOf(
        TitipSession(
            id = 1,
            title = "Mcd Jakal",
            recipientName = "Fulan",
            location = "Mcdonald's Jakal",
            category = "Makanan/Minuman",
            iconRes = R.drawable.ic_makanan,
            status = TitipStatus.MENUNGGU_ACCEPT,
            amount = "-",
            requestItem = "Lemon Tea",
            requestQty = 1
        ),
        TitipSession(
            id = 2,
            title = "Belanja Indomaret",
            recipientName = "Budi",
            location = "Indomaret Jakal",
            category = "Belanjaan",
            iconRes = R.drawable.ic_belanja,
            status = TitipStatus.DIPROSES,
            amount = "-",
            requestItem = "Roti",
            requestQty = 2
        ),
        TitipSession(
            id = 3,
            title = "Beli Snack",
            recipientName = "Andi",
            location = "Alfamart",
            category = "Belanjaan",
            iconRes = R.drawable.ic_belanja,
            status = TitipStatus.DITOLAK,
            amount = "-",
            requestItem = "Chitato",
            requestQty = 1
        ),
        TitipSession(
            id = 4,
            title = "Beli Obat",
            recipientName = "Cici",
            location = "Apotek K24",
            category = "Obat-obatan",
            iconRes = R.drawable.ic_obat,
            status = TitipStatus.BAYAR_DAN_ANTAR,
            amount = "Rp 100.000",
            requestItem = "Paracetamol",
            requestQty = 1
        )
    )
    
    private fun getDummyDititipiSessions() = listOf(
        DititipiSession(
            id = 2,
            title = "Pizza Hut",
            location = "Pizza Hut Hartono Mall",
            category = "Makanan/Minuman",
            iconRes = R.drawable.ic_makanan,
            participantCount = 5,
            status = DititipiStatus.MENUNGGU_PESANAN,
            timeRemaining = "01:30:07"
        ),
        DititipiSession(
            id = 1,
            title = "Alfamart Jakal",
            location = "Alfamart Pogung",
            category = "Belanjaan",
            iconRes = R.drawable.ic_belanja,
            participantCount = 3,
            status = DititipiStatus.BELANJA,
            timeRemaining = "00:45:23"
        )
    )
    
    fun updateSessionStatus(id: Int, status: TitipStatus) {
        val updated = _titipSessions.value.map {
            if (it.id == id) it.copy(status = status) else it
        }
        _titipSessions.value = updated
    }
}
