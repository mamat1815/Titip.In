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

    // --- STATE ---
    var circleState by mutableStateOf<Circle?>(null)

    // Kita pisahkan Sesi Aktif dan Riwayat agar UI lebih mudah
    var activeSession by mutableStateOf<JastipSession?>(null)
    var sessionHistory by mutableStateOf<List<JastipSession>>(emptyList())

    // State Timer
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
                    // 1. Ambil semua sesi yang statusnya 'open'
                    val openSessions = allSessions.filter { it.status == "open" }

                    // 2. LOGIC SINGLE SESSION:
                    // Jika ada error data (misal 2 sesi open), kita paksa ambil yang paling baru saja.
                    // Ini memastikan di UI hanya ada 1 sesi yang berjalan.
                    activeSession = openSessions.maxByOrNull { it.createdAt }

                    // 3. Sisanya masuk ke riwayat (termasuk sesi open yang 'kalah'/double)
                    sessionHistory = allSessions.filter { it.id != activeSession?.id }

                    // 4. Jalankan Timer jika ada sesi aktif
                    if (activeSession != null) {
                        startTimer(activeSession!!)
                    } else {
                        stopTimer()
                    }
                }
                result.onFailure {
                    errorMessage = "Gagal memuat sesi: ${it.message}"
                }
            }
        }
    }

    // --- LOGIC TIMER HITUNG MUNDUR ---
    private fun startTimer(session: JastipSession) {
        // Cancel timer lama jika ada
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
                        // Waktu habis
                        break
                    }
                    delay(1000) // Update setiap 1 detik
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