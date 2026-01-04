package com.afsar.titipin.data.remote.repository.session

import com.afsar.titipin.data.model.ChatMessage
import com.afsar.titipin.data.model.Order
import com.afsar.titipin.data.model.PaymentInfo
import com.afsar.titipin.data.model.Session
import kotlinx.coroutines.flow.Flow

interface SessionRepository {

    fun createSession(session: Session): Flow<Result<Boolean>>

    fun getListSession(circleId: String): Flow<Result<List<Session>>>
    fun getSessionById( sessionId: String): Flow<Result<Session>>
    fun updateSession(circleId: String, sessionId: String, session: Session): Flow<Result<Boolean>>
    fun deleteSession(circleId: String, sessionId: String): Flow<Result<Boolean>>
    fun getSessionChatMessages(sessionId: String): Flow<List<ChatMessage>>
    fun getMySessions(): Flow<Result<List<Session>>>
    fun getOneMySession(): Flow<Result<Session>>


    fun sendSessionChatMessage(circleId: String, sessionId: String, message: String): Flow<Result<Boolean>>
    fun listenToPaymentsBySessionAndUser(sessionId: String, userId: String): Flow<Result<List<PaymentInfo>>>
}