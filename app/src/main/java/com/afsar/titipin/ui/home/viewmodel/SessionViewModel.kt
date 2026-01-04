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
    private val sessionRepository: SessionRepository,
    private val orderRepository: OrderRepository
) : ViewModel() {

    var mySessions by mutableStateOf<List<Session>>(emptyList())
    var myOrders by mutableStateOf<List<Order>>(emptyList())

    init {
        loadMySessions()
        loadMyOrders()
    }

    fun loadMySessions() {
        viewModelScope.launch {
            sessionRepository.getMySessions().collect { result ->
                result.onSuccess{ mySessions = it}
            }
        }
    }

    fun loadMyOrders(){
        viewModelScope.launch {
            orderRepository.getMyOrders().collect { result ->
                result.onSuccess { myOrders = it }
            }
        }
    }
}