//package com.afsar.titipin.ui.circle.detail
//
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.setValue
//import androidx.lifecycle.SavedStateHandle
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.afsar.titipin.data.model.Circle
//import com.afsar.titipin.data.model.Session
//import com.afsar.titipin.data.remote.AuthRepository
//import dagger.hilt.android.lifecycle.HiltViewModel
//import kotlinx.coroutines.Job
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.launch
//import javax.inject.Inject
//
//@HiltViewModel
//class CircleDetailViewModel @Inject constructor(
//    private val repository: AuthRepository,
//    savedStateHandle: SavedStateHandle
//) : ViewModel() {
//
//    private val circleId: String = checkNotNull(savedStateHandle["circleId"])
//    var circleState by mutableStateOf<Circle?>(null)
//
//    var activeSession by mutableStateOf<Session?>(null)
//    var sessionHistory by mutableStateOf<List<Session>>(emptyList())
//
//    var remainingTime by mutableStateOf("")
//    private var timerJob: Job? = null
//
//    var isLoading by mutableStateOf(false)
//    var errorMessage by mutableStateOf<String?>(null)
//
//
//    init {
//        loadData(circleId)
//    }
//
//    private fun loadData(id: String) {
//        isLoading = true
//        refreshCircleDetail(id)
//        fetchSessions(id)
//    }
//
//    fun refreshCircleDetail(circleId: String) {
//        viewModelScope.launch {
//            repository.getCircleDetail(circleId).collect { result ->
//                result.onSuccess { circleState = it }
//            }
//        }
//    }
//
//    fun fetchSessions(circleId: String) {
//        viewModelScope.launch {
//            repository.getCircleSessions(circleId).collect { result ->
//                result.onSuccess { allSessions ->
//                    val latest = allSessions.maxByOrNull { it.createdAt }
//
//                    activeSession = latest
//
//                    sessionHistory = allSessions.filter { it.id != activeSession?.id }
//
//                    if (activeSession != null && activeSession?.status == "open") {
//                        startTimer(activeSession!!)
//                    } else {
//                        stopTimer()
//                        if (activeSession != null) {
//                            remainingTime = "Selesai"
//                        }
//                    }
//                }
//                result.onFailure {
//                    errorMessage = "Gagal memuat sesi: ${it.message}"
//                }
//            }
//        }
//    }
//
//    private fun startTimer(session: Session) {
//        timerJob?.cancel()
//
//        timerJob = viewModelScope.launch {
//            while (true) {
//                try {
//                    val createdTime = session.createdAt.toDate().time
//                    val durationMillis = session.durationMinutes * 60 * 1000L
//                    val endTime = createdTime + durationMillis
//                    val currentTime = System.currentTimeMillis()
//                    val timeLeft = endTime - currentTime
//
//                    if (timeLeft > 0) {
//                        val minutes = (timeLeft / 1000) / 60
//                        val seconds = (timeLeft / 1000) % 60
//                        remainingTime = String.format("%02d:%02d", minutes, seconds)
//                    } else {
//                        remainingTime = "00:00"
//                        val currentUid = repository.getCurrentUserUid()
//                        if (currentUid == session.creatorId && session.status == "open") {
//                            repository.updateSessionStatus(session.id, "closed").collect {
//                                // Status updated
//                            }
//                        }
//                        break
//                    }
//                    delay(1000)
//                } catch (e: Exception) {
//                    remainingTime = "00:00"
//                    break
//                }
//            }
//        }
//    }
//
//    private fun stopTimer() {
//        timerJob?.cancel()
//        remainingTime = ""
//    }
//}
//
//package com.afsar.titipin.ui.circle.detail
//
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.setValue
//import androidx.lifecycle.SavedStateHandle
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.afsar.titipin.data.model.Circle
//import com.afsar.titipin.data.model.Session
//import com.afsar.titipin.data.model.User
//import com.afsar.titipin.data.remote.repository.circle.CircleRepository
//import com.afsar.titipin.data.remote.repository.session.SessionRepository
//import com.afsar.titipin.data.remote.AuthRepository // Untuk ambil data user/member
//import dagger.hilt.android.lifecycle.HiltViewModel
//import kotlinx.coroutines.Job
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.launch
//import javax.inject.Inject
//
//@HiltViewModel
//class CircleDetailViewModel @Inject constructor(
//    private val circleRepository: CircleRepository,   // Ganti ke Repo yang benar
//    private val sessionRepository: SessionRepository, // Ganti ke Repo yang benar
//    private val authRepository: AuthRepository,       // Untuk ambil detail member
//    savedStateHandle: SavedStateHandle
//) : ViewModel() {
//
//    private val circleId: String = checkNotNull(savedStateHandle["circleId"])
//
//    // State Data
//    var circleState by mutableStateOf<Circle?>(null)
//    var membersState by mutableStateOf<List<User>>(emptyList()) // List Member User
//    var activeSession by mutableStateOf<Session?>(null)
//    var sessionHistory by mutableStateOf<List<Session>>(emptyList())
//
//    // State Timer & UI
//    var remainingTime by mutableStateOf("")
//    private var timerJob: Job? = null
//    var isLoading by mutableStateOf(false)
//    var errorMessage by mutableStateOf<String?>(null)
//
//    init {
//        loadData()
//    }
//
//    private fun loadData() {
//        isLoading = true
//        // 1. Ambil Detail Circle
//        viewModelScope.launch {
//            circleRepository.getCircleDetail(circleId).collect { result ->
//                result.onSuccess { circle ->
//                    circleState = circle
//                    // Setelah dapat Circle, ambil data Member berdasarkan ID
//                    fetchMembers(circle.memberIds)
//                }
//                result.onFailure { errorMessage = it.message }
//            }
//        }
//
//        // 2. Ambil Daftar Sesi
//        viewModelScope.launch {
//            sessionRepository.getListSession(circleId).collect { result ->
//                result.onSuccess { allSessions ->
//                    // Pisahkan Sesi Aktif dan History
//                    // Prioritas: Cari yang statusnya "open"
//                    activeSession = allSessions.firstOrNull { it.status == "open" }
//
//                    // Sisanya masuk history
//                    sessionHistory = allSessions.filter { it.id != activeSession?.id }
//
//                    // Jalankan Timer jika ada sesi aktif
//                    if (activeSession != null) {
//                        startTimer(activeSession!!)
//                    } else {
//                        stopTimer()
//                    }
//                }
//                result.onFailure { errorMessage = it.message }
//            }
//        }
//    }
//
//    private fun fetchMembers(memberIds: List<String>) {
//        if (memberIds.isEmpty()) return
//
//        viewModelScope.launch {
//            // Asumsi AuthRepository punya fungsi getUsersByIds.
//            // Jika belum, Anda perlu membuatnya menggunakan query 'whereIn' Firestore
//            authRepository.getUsersByIds(memberIds).collect { result ->
//                result.onSuccess { users ->
//                    membersState = users
//                }
//            }
//        }
//    }
//
//    private fun startTimer(session: Session) {
//        timerJob?.cancel()
//
//        // Cek null safety untuk createdAt (jaga-jaga server timestamp belum sync)
//        if (session.createdAt == null) return
//
//        timerJob = viewModelScope.launch {
//            while (true) {
//                try {
//                    val createdTime = session.createdAt.toDate().time
//                    val durationMillis = session.durationMinutes * 60 * 1000L
//                    val endTime = createdTime + durationMillis
//                    val currentTime = System.currentTimeMillis()
//                    val timeLeft = endTime - currentTime
//
//                    if (timeLeft > 0) {
//                        val minutes = (timeLeft / 1000) / 60
//                        val seconds = (timeLeft / 1000) % 60
//                        remainingTime = String.format("%02d:%02d", minutes, seconds)
//                    } else {
//                        remainingTime = "00:00"
//                        // Otomatis tutup sesi sebaiknya dilakukan oleh Cloud Functions (Backend)
//                        // atau user trigger manual agar tidak terjadi race condition di UI.
//                        break
//                    }
//                    delay(1000)
//                } catch (e: Exception) {
//                    remainingTime = "Error"
//                    break
//                }
//            }
//        }
//    }
//
//    private fun stopTimer() {
//        timerJob?.cancel()
//        remainingTime = ""
//    }
//}

package com.afsar.titipin.ui.circle.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afsar.titipin.data.model.Circle
import com.afsar.titipin.data.model.Session
import com.afsar.titipin.data.model.User
import com.afsar.titipin.data.remote.AuthRepository
import com.afsar.titipin.data.remote.repository.circle.CircleRepository
import com.afsar.titipin.data.remote.repository.session.SessionRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import javax.inject.Inject

@HiltViewModel
class CircleDetailViewModel @Inject constructor(
    private val circleRepository: CircleRepository,
    private val sessionRepository: SessionRepository,
    private val authRepository: AuthRepository,
    private val firebaseAuth: FirebaseAuth, // Inject ini untuk cek UID sendiri saat auto-close
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val circleId: String = checkNotNull(savedStateHandle["circleId"])

    // --- STATE UI ---
    var circleState by mutableStateOf<Circle?>(null)
    var membersState by mutableStateOf<List<User>>(emptyList())

    var activeSession by mutableStateOf<Session?>(null)
    var sessionHistory by mutableStateOf<List<Session>>(emptyList())

    var remainingTime by mutableStateOf("")
    private var timerJob: Job? = null

    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    init {
        loadData()
    }

    private fun loadData() {
        isLoading = true

        // 1. Ambil Data Circle
        viewModelScope.launch {
            circleRepository.getCircleDetail(circleId).collect { result ->
                result.onSuccess { circle ->
                    circleState = circle
                    // Setelah circle didapat, ambil data profil membernya
                    fetchMembers(circle.memberIds)
                }
                result.onFailure {
                    errorMessage = "Gagal memuat circle: ${it.message}"
                }
            }
        }

        // 2. Ambil Data Session
        viewModelScope.launch {
            sessionRepository.getListSession(circleId).collect { result ->
                result.onSuccess { allSessions ->
                    // Logic: Cari sesi yang statusnya "open"
                    val openSession = allSessions.firstOrNull { it.status == "open" }

                    // Logic: Jika tidak ada yg open, ambil yang paling baru dibuat
                    // (Opsional, tergantung kebutuhan. Di sini kita prioritaskan yang OPEN jadi Active)
                    activeSession = openSession

                    // Sisanya masuk ke history
                    sessionHistory = allSessions.filter { it.id != activeSession?.id }

                    // Jalankan timer hanya jika ada sesi aktif yang OPEN
                    if (activeSession != null && activeSession!!.status == "open") {
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
                isLoading = false
            }
        }
    }

    private fun fetchMembers(memberIds: List<String>) {
        if (memberIds.isEmpty()) return

        // Pastikan AuthRepository punya fungsi ini.
        // Jika belum, buat fungsi getUsersByIds(ids: List<String>) di AuthRepository
        viewModelScope.launch {
            authRepository.getUsersByIds(memberIds).collect { result ->
                result.onSuccess { users ->
                    membersState = users
                }
            }
        }
    }

    private fun startTimer(session: Session) {
        timerJob?.cancel()

        // Guard clause: Jika createdAt null (belum sync server), jangan jalankan timer dulu
        val createdAt = session.createdAt ?: return

        timerJob = viewModelScope.launch {
            while (true) {
                try {
                    val createdTime = createdAt.toDate().time
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

                        // --- LOGIKA AUTO CLOSE ---
                        // Jika waktu habis, dan user yg login adalah pembuat sesi,
                        // maka otomatis update status ke "closed" di database.
                        val myUid = firebaseAuth.currentUser?.uid
                        if (myUid != null && myUid == session.creatorId && session.status == "open") {
                            closeSession(session)
                        }
                        break
                    }
                    delay(1000)
                } catch (e: Exception) {
                    remainingTime = "Error"
                    break
                }
            }
        }
    }

    private fun closeSession(session: Session) {
        viewModelScope.launch {
            // Kita update object session, set status jadi closed
            val closedSession = session.copy(status = "closed")

            // Panggil Repository untuk update
            sessionRepository.updateSession(
                circleId = session.circleId,
                sessionId = session.id,
                session = closedSession
            ).collect {
                // Tidak perlu handle success UI disini karena snapshot listener
                // di loadData() akan otomatis merefresh UI ketika data di DB berubah.
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        remainingTime = ""
    }
}