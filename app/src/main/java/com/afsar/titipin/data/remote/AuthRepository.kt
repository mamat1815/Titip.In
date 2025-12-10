package com.afsar.titipin.data.remote

import com.afsar.titipin.data.model.ChatMessage
import com.afsar.titipin.data.model.Circle
import com.afsar.titipin.data.model.CircleRequest
import com.afsar.titipin.data.model.JastipOrder
import com.afsar.titipin.data.model.JastipSession
import com.afsar.titipin.data.model.PaymentInfo
import com.afsar.titipin.data.model.User
import com.google.firebase.auth.AuthResult
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

//    Authentication
    fun login(email: String, pass: String): Flow<Result<AuthResult>>
    fun register(name: String, username: String, email: String, pass: String): Flow<Result<AuthResult>>
    fun loginWithGoogle(idToken: String): Flow<Result<AuthResult>>
    fun logout()

    fun getUserProfile(): Flow<Result<User>>
    fun searchUsers(query: String): Flow<Result<List<User>>>
    
    // Bank Account
    fun updateBankAccount(bankName: String, bankAccountNumber: String, bankAccountName: String): Flow<Result<Boolean>>

    fun sendCircleRequest(receiverId: String): Flow<Result<Boolean>>
    fun getIncomingRequests(): Flow<Result<List<CircleRequest>>>
    fun respondToRequest(requestId: String, isAccepted: Boolean): Flow<Result<Boolean>>
    fun createCircle(name: String, members: List<User>): Flow<Result<Boolean>>
    fun getMyCircles(): Flow<Result<List<Circle>>>
    fun getCircleDetail(circleId: String): Flow<Result<Circle>>
    fun createJastipSession(session: JastipSession): Flow<Result<Boolean>>
    fun getCircleSessions(circleId: String): Flow<Result<List<JastipSession>>>
    fun getMyCircle(): Flow<Result<List<User>>>
    fun getMyJastipSessions(): Flow<Result<List<JastipSession>>>
    fun getSessionOrders(sessionId: String): Flow<Result<List<JastipOrder>>>
    fun updateOrderStatus(orderId: String, newStatus: String): Flow<Result<Boolean>>
    fun createJastipOrder(order: JastipOrder): Flow<Result<Boolean>>
    fun getSessionChatMessages(sessionId: String): Flow<Result<List<ChatMessage>>>
    fun sendSessionChatMessage(sessionId: String, message: String): Flow<Result<Boolean>>
    fun getCurrentUserUid(): String?
    fun updateSessionStatus(sessionId: String, newStatus: String): Flow<Result<Boolean>>
    fun toggleRevisionMode(sessionId: String, isRevision: Boolean): Flow<Result<Boolean>>
    fun listenToPaymentsBySessionAndUser(sessionId: String, userId: String): Flow<Result<List<PaymentInfo>>>
}