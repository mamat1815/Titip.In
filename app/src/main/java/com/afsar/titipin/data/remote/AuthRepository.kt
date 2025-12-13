package com.afsar.titipin.data.remote

import com.afsar.titipin.data.model.Bank
import com.afsar.titipin.data.model.ChatMessage
import com.afsar.titipin.data.model.Circle
import com.afsar.titipin.data.model.CircleRequest
import com.afsar.titipin.data.model.Order
import com.afsar.titipin.data.model.Session
import com.afsar.titipin.data.model.PaymentInfo
import com.afsar.titipin.data.model.User
import com.google.firebase.auth.AuthResult
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

//    Authentication
    fun login(email: String, pass: String): Flow<Result<AuthResult>>
    fun register(name: String, username: String, email: String, pass: String): Flow<Result<AuthResult>>
    fun loginWithGoogle(idToken: String): Flow<Result<AuthResult>>
    fun getCurrentUserUid(): String?
    fun logout()
    fun getUserProfile(): Flow<Result<User>>
    fun searchUsers(query: String): Flow<Result<List<User>>>
    
    // Bank Account
    fun updateBankAccount(bankName: String, bankAccountNumber: String, bankAccountName: String): Flow<Result<Boolean>>

    //    Circle
    fun createCircle(name: String, members: List<User>): Flow<Result<Boolean>>
    fun getMyCircles(): Flow<Result<List<Circle>>>
    fun getCircleDetail(circleId: String): Flow<Result<Circle>>

    //  Todo Implementasi Dari Accept dan Reject Request
    fun sendCircleRequest(receiverId: String): Flow<Result<Boolean>>
    fun getIncomingRequests(): Flow<Result<List<CircleRequest>>>
    fun respondToRequest(requestId: String, isAccepted: Boolean): Flow<Result<Boolean>>


    //    Session
    fun createJastipSession(session: Session): Flow<Result<Boolean>>
    fun getUsersByIds(uids: List<String>): Flow<Result<List<User>>>
    fun getCircleSessions(circleId: String): Flow<Result<List<Session>>>

    fun getMyJastipSessions(): Flow<Result<List<Session>>>

    fun getSessionOrders(sessionId: String): Flow<Result<List<Order>>>

//    fun sendSessionChatMessage(sessionId: String, message: String): Flow<Result<Boolean>>
    fun updateSessionStatus(sessionId: String, newStatus: String): Flow<Result<Boolean>>
    fun toggleRevisionMode(sessionId: String, isRevision: Boolean): Flow<Result<Boolean>>
    fun updateBankAccount(bank: Bank): Flow<Result<Boolean>>
    //    Order
    fun createJastipOrder(order: Order): Flow<Result<Boolean>>
    fun updateOrderStatus(orderId: String, newStatus: String): Flow<Result<Boolean>>
    fun getSessionChatMessages(sessionId: String): Flow<Result<List<ChatMessage>>>
    fun listenToPaymentsBySessionAndUser(sessionId: String, userId: String): Flow<Result<List<PaymentInfo>>>

//    Payment



}