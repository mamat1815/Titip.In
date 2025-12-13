package com.afsar.titipin.data.remote.repository.order

import com.afsar.titipin.data.model.Order
import com.afsar.titipin.data.model.Session
import kotlinx.coroutines.flow.Flow

interface OrderRepository {

    fun createOrder(circleId: String, sessionId: String, order: Order): Flow<Result<Boolean>>
    fun getListOrder(circleId: String, sessionId: String): Flow<Result<List<Order>>>
    fun getOrderById(circleId: String, sessionId: String, orderId: String): Flow<Result<Order>>
    fun updateOrder(circleId: String, sessionId: String, orderId: String, order: Order): Flow<Result<Boolean>>
    fun updateOrderStatus(circleId: String, sessionId: String, orderId: String, newStatus: String): Flow<Result<Boolean>>
    fun deleteOrder(circleId: String, sessionId: String, orderId: String): Flow<Result<Boolean>>
    fun getOrdersBySession(sessionId: String): Flow<Result<List<Order>>>
    fun getMyOrders(userId: String): Flow<Result<List<Order>>>
}