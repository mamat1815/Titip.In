package com.afsar.titipin.ui.home.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afsar.titipin.data.model.ChatMessage
import com.afsar.titipin.data.model.ChatMessages
import com.afsar.titipin.data.remote.ChatCircleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.text.get

@HiltViewModel
class ChatCircleViewModel @Inject constructor(
    private val repository: ChatCircleRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Ambil circleId dari argument navigasi
    private val circleId: String = checkNotNull(savedStateHandle["circleId"])
    var memberCount by mutableStateOf(0)
        private set

    // State untuk UI
    var messages = mutableStateListOf<ChatMessages>()
        private set

    var circleName by mutableStateOf("Loading...")
        private set

    var circleAvatar by mutableStateOf("")
        private set

    val currentUserId: String get() = repository.currentUserId ?: ""

    init {
        loadCircleInfo()
        listenToMessages()
    }

    // Load Nama & Foto Circle
    private fun loadCircleInfo() {
        viewModelScope.launch {
            val info = repository.getCircleInfo(circleId)
            circleName = info["name"] as String
            circleAvatar = info["avatarUrl"] as String
            memberCount = info["memberCount"] as Int // Ambil data count
        }
    }

    // Listen Chat Realtime
    private fun listenToMessages() {
        viewModelScope.launch {
            repository.getMessages(circleId).collect { newMessages ->
                messages.clear()
                messages.addAll(newMessages)
            }
        }
    }

    // Kirim Pesan
    fun sendMessage(text: String) {
        if (text.isBlank()) return

        viewModelScope.launch {
            // Optimistic update (opsional): Bisa tambahkan pesan dummy dulu biar UI responsif
            // Tapi karena Firestore cepat, langsung panggil repo juga oke.
            repository.sendMessage(circleId, text)
        }
    }
}