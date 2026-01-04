package com.afsar.titipin.ui.home.viewmodel

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afsar.titipin.data.model.Circle
import com.afsar.titipin.data.model.User
import com.afsar.titipin.data.remote.AuthRepository
import com.afsar.titipin.data.remote.repository.circle.CircleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CircleViewModel @Inject constructor(
    private val repository: CircleRepository
) : ViewModel() {

    // State dipisah agar UI bisa bereaksi terhadap Loading & Error
    var myCircles by mutableStateOf<List<Circle>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        loadMyCircles()
    }

    fun loadMyCircles() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            repository.getMyCircles().collect { result ->
                isLoading = false // Stop loading saat data diterima

                result.onSuccess { circles ->
                    myCircles = circles
                    errorMessage = null
                }
                result.onFailure { error ->
                    errorMessage = error.message ?: "Gagal memuat circle"
                }
            }
        }
    }
}


@HiltViewModel
class CircleListViewModel @Inject constructor(
    private val circleRepository: CircleRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    // --- STATE LIST CIRCLE ---
    var circles by mutableStateOf<List<Circle>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)

    // --- STATE CREATE CIRCLE (BOTTOM SHEET) ---
    // List kontak hasil search
    var searchContactResults = mutableStateListOf<User>()
    // List kontak yang dipilih
    var selectedContacts = mutableStateListOf<User>()

    var isCreating by mutableStateOf(false)
    private var searchJob: Job? = null

    init {
        loadMyCircles()
    }

    // 1. Load List Circle Saya
    fun loadMyCircles() {
        viewModelScope.launch {
            isLoading = true
            circleRepository.getMyCircles().collect { result ->
                isLoading = false
                result.onSuccess { circles = it }
            }
        }
    }

    // 2. Search User untuk ditambahkan ke Circle
    fun searchContacts(query: String) {
        searchJob?.cancel()
        if (query.isBlank()) {
            searchContactResults.clear()
            return
        }

        searchJob = viewModelScope.launch {
            delay(500) // Debounce
            authRepository.searchUsers(query).collect { result ->
                result.onSuccess { users ->
                    // Filter user yang sudah dipilih & user sendiri
                    val currentUid = authRepository.getCurrentUserUid()
                    searchContactResults.clear()
                    searchContactResults.addAll(
                        users.filter { user ->
                            user.uid != currentUid && selectedContacts.none { it.uid == user.uid }
                        }
                    )
                }
            }
        }
    }

    // 3. Toggle Selection
    fun toggleContactSelection(user: User) {
        if (selectedContacts.any { it.uid == user.uid }) {
            selectedContacts.removeIf { it.uid == user.uid }
        } else {
            selectedContacts.add(user)
        }
        // Bersihkan hasil search agar rapi
        searchContactResults.clear()
    }

    fun removeContact(user: User) {
        selectedContacts.remove(user)
    }

    // 4. Reset Form saat Sheet dibuka/tutup
    fun resetSelection() {
        selectedContacts.clear()
        searchContactResults.clear()
    }

    // 5. Create Circle
    fun createCircle(name: String, imageUri: Uri?, onSuccess: () -> Unit) {
        if (name.isBlank()) return

        isCreating = true
        viewModelScope.launch {
            // Note: imageUri belum diproses karena butuh Firebase Storage.
            // Untuk sekarang kita kirim nama & member saja.

            circleRepository.createCircle(name, selectedContacts.toList()).collect { result ->
                isCreating = false
                result.onSuccess {
                    resetSelection()
                    loadMyCircles() // Refresh list
                    onSuccess()
                }
            }
        }
    }
}