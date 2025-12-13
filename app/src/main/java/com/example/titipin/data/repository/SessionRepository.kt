package com.example.titipin.data.repository

import com.example.titipin.R
import com.example.titipin.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionRepository {
    private val _participants = MutableStateFlow(getDummyParticipants())
    val participants: StateFlow<List<ParticipantRequest>> = _participants.asStateFlow()
    
    private fun getDummyParticipants() = listOf(
        ParticipantRequest(
            id = 1,
            name = "Budi Santoso",
            circleName = "Teman Kantor",
            orderItems = listOf(
                OrderItem("Air Le mineral", 1),
                OrderItem("Indomie Goreng", 2),
                OrderItem("Teh Pucuk Harum", 1)
            ),
            notes = "Indomie nya rasa rendang",
            amount = "Rp 25.000",
            status = RequestStatus.PENDING,
            avatarRes = R.drawable.ic_profile1
        ),
        ParticipantRequest(
            id = 2,
            name = "Citra Lestari",
            circleName = "Mabar Valorant",
            orderItems = listOf(
                OrderItem("Chocolatos Matcha Sachet", 3),
                OrderItem("Nabati Richeese", 2)
            ),
            notes = "-",
            amount = "Rp 30.000",
            status = RequestStatus.PENDING,
            avatarRes = R.drawable.ic_profile2
        ),
        ParticipantRequest(
            id = 3,
            name = "Eko Wibowo",
            circleName = "Anak Fasilkom",
            orderItems = listOf(
                OrderItem("Aqua Botol", 2),
                OrderItem("Roti Aoka Coklat", 1)
            ),
            notes = "",
            amount = "Rp 15.000",
            status = RequestStatus.ACCEPTED,
            avatarRes = R.drawable.ic_profile1
        )
    )
    
    fun acceptRequest(id: Int) {
        val updated = _participants.value.map {
            if (it.id == id) it.copy(status = RequestStatus.ACCEPTED) else it
        }
        _participants.value = updated
    }
    
    fun rejectRequest(id: Int) {
        val updated = _participants.value.map {
            if (it.id == id) it.copy(status = RequestStatus.REJECTED) else it
        }
        _participants.value = updated
    }
}
