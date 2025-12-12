package com.afsar.titipin.ui.home.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afsar.titipin.data.model.Circle
import com.afsar.titipin.data.model.CircleRequest
import com.afsar.titipin.data.model.User
import com.afsar.titipin.data.remote.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.plus

@HiltViewModel
class CircleViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    // --- LIST CIRCLE (Home Circle) ---
    var myCircles by mutableStateOf<List<Circle>>(emptyList())

    // --- CREATE CIRCLE STATE ---
    var newCircleName by mutableStateOf("")
    var searchQuery by mutableStateOf("")
    var searchResults by mutableStateOf<List<User>>(emptyList())
    var selectedMembers by mutableStateOf<List<User>>(emptyList())
    var isCreating by mutableStateOf(false)

    private var searchJob: Job? = null

    init {
        loadMyCircles()
    }

    fun loadMyCircles() {
        viewModelScope.launch {
            repository.getMyCircles().collect { result ->
                result.onSuccess { myCircles = it }
            }
        }
    }

    // --- SEARCH USER LOGIC ---
    fun onSearchQueryChange(query: String) {
        searchQuery = query
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500)
            if (query.isNotEmpty()) {
                repository.searchUsers(query).collect { result ->
                    result.onSuccess { searchResults = it }
                }
            } else {
                searchResults = emptyList()
            }
        }
    }

    // Tambah member ke list seleksi
    fun addMemberToSelection(user: User) {
        if (!selectedMembers.contains(user)) {
            selectedMembers = selectedMembers + user
        }
        // Reset search biar bersih
        searchQuery = ""
        searchResults = emptyList()
    }

    // Hapus member dari seleksi
    fun removeMemberFromSelection(user: User) {
        selectedMembers = selectedMembers - user
    }

    // Create Circle Final
    fun createCircle(onSuccess: () -> Unit) {
        if (newCircleName.isBlank()) return

        isCreating = true
        viewModelScope.launch {
            repository.createCircle(newCircleName, selectedMembers).collect {
                isCreating = false
                onSuccess()
                // Reset State
                newCircleName = ""
                selectedMembers = emptyList()
                loadMyCircles() // Refresh list utama
            }
        }
    }

    var isSearching by mutableStateOf(false)

    // --- Request State ---
    var incomingRequests by mutableStateOf<List<CircleRequest>>(emptyList())


//    init {
//        loadIncomingRequests()
//    }
//
//
//    fun loadIncomingRequests() {
//        viewModelScope.launch {
//            repository.getIncomingRequests().collect { result ->
//                result.onSuccess { incomingRequests = it }
//            }
//        }
//    }


}