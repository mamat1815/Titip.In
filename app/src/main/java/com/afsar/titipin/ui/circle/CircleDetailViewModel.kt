package com.afsar.titipin.ui.circle

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afsar.titipin.data.model.Circle
import com.afsar.titipin.data.model.JastipSession
import com.afsar.titipin.data.remote.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CircleDetailViewModel @Inject constructor(
    private val repository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    var circleState by mutableStateOf<Circle?>(null)

    var activeSession by mutableStateOf<JastipSession?>(null)
    var sessionHistory by mutableStateOf<List<JastipSession>>(emptyList())

    var remainingTime by mutableStateOf("")
    private var timerJob: Job? = null

    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    init {
        val initialData = savedStateHandle.get<Circle>("EXTRA_CIRCLE_DATA")

        if (initialData != null) {
            circleState = initialData
            refreshCircleDetail(initialData.id)
            fetchSessions(initialData.id)
        }
    }

    fun refreshCircleDetail(circleId: String) {
        viewModelScope.launch {
            repository.getCircleDetail(circleId).collect { result ->
                result.onSuccess { circleState = it }
            }
        }
    }

    fun fetchSessions(circleId: String) {
        viewModelScope.launch {
            repository.getCircleSessions(circleId).collect { result ->
                result.onSuccess { allSessions ->
                    val latest = allSessions.maxByOrNull { it.createdAt }

                    activeSession = latest

                    sessionHistory = allSessions.filter { it.id != activeSession?.id }

                    if (activeSession != null && activeSession?.status == "open") {
                        startTimer(activeSession!!)
                    } else {
                        stopTimer()
                        if (activeSession != null) {
                            remainingTime = "Selesai"
                        }
                    }
                }
                result.onFailure {
                    errorMessage = "Gagal memuat sesi: ${it.message}"
                }
            }
        }
    }

    private fun startTimer(session: JastipSession) {
        timerJob?.cancel()

        timerJob = viewModelScope.launch {
            while (true) {
                try {
                    val createdTime = session.createdAt.toDate().time
                    val durationMillis = session.durationMinutes * 60 * 1000L
                    val endTime = createdTime + durationMillis
                    val currentTime = System.currentTimeMillis()
                    val timeLeft = endTime - currentTime

                    if (timeLeft > 0) {
                        val minutes = (timeLeft / 1000) / 60
                        val seconds = (timeLeft / 1000) % 60
                        remainingTime = String.format("%02d:%02d", minutes, seconds)
                    } else {
                        remainingTime = "00:00"
                        val currentUid = repository.getCurrentUserUid()
                        if (currentUid == session.creatorId && session.status == "open") {
                            repository.updateSessionStatus(session.id, "closed").collect {
                                // Status updated
                            }
                        }
                        break
                    }
                    delay(1000)
                } catch (e: Exception) {
                    remainingTime = "00:00"
                    break
                }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        remainingTime = ""
    }
}