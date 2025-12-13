package com.afsar.titipin.ui.circle.add

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afsar.titipin.data.model.User
import com.afsar.titipin.data.remote.AuthRepository
import com.afsar.titipin.data.remote.repository.circle.CircleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddCircleViewModel @Inject constructor(
    private val authRepository: AuthRepository, // Pastikan ada repository untuk search user
    private val circleRepository: CircleRepository
) : ViewModel() {

    // State UI
    var newCircleName by mutableStateOf("")
    var searchQuery by mutableStateOf("")
    var searchResults by mutableStateOf<List<User>>(emptyList())
    var selectedMembers by mutableStateOf<List<User>>(emptyList())

    // State Loading & Error
    var isCreating by mutableStateOf(false)
    var isSearching by mutableStateOf(false) // Tambahan indikator loading search
    var errorMessage by mutableStateOf<String?>(null)

    private var searchJob: Job? = null

    fun onSearchQueryChange(query: String) {
        searchQuery = query
        searchJob?.cancel()

        if (query.isEmpty()) {
            searchResults = emptyList()
            return
        }

        searchJob = viewModelScope.launch {
            delay(500) // Debounce (tunggu ketikan selesai)
            isSearching = true

            // Asumsi authRepository.searchUsers mengembalikan Flow<Result<List<User>>>
            authRepository.searchUsers(query).collect { result ->
                isSearching = false
                result.onSuccess { users ->
                    // Filter user yang sudah dipilih agar tidak muncul lagi di hasil pencarian (Opsional)
                    searchResults = users.filter { user ->
                        selectedMembers.none { it.uid == user.uid }
                    }
                }
                result.onFailure {
                    // Opsional: Handle error search diam-diam atau tampilkan snackbar
                    searchResults = emptyList()
                }
            }
        }
    }

    fun addMemberToSelection(user: User) {
        // Cek duplikasi sebelum add (walaupun UI sudah memfilter)
        if (selectedMembers.none { it.uid == user.uid }) {
            selectedMembers = selectedMembers + user
        }
        // Reset search setelah memilih
        searchQuery = ""
        searchResults = emptyList()
    }

    fun removeMemberFromSelection(user: User) {
        selectedMembers = selectedMembers - user
    }

    fun createCircle(onSuccess: () -> Unit) {
        if (newCircleName.isBlank()) {
            errorMessage = "Nama circle tidak boleh kosong"
            return
        }

        isCreating = true
        errorMessage = null // Reset error sebelumnya

        viewModelScope.launch {
            circleRepository.createCircle(newCircleName, selectedMembers).collect { result ->
                isCreating = false

                // --- BAGIAN INI YANG DIPERBAIKI ---
                result.onSuccess {
                    resetForm()
                    onSuccess() // Panggil navigasi hanya jika sukses
                }
                result.onFailure { error ->
                    errorMessage = error.message ?: "Gagal membuat circle"
                }
            }
        }
    }

    private fun resetForm() {
        newCircleName = ""
        selectedMembers = emptyList()
        searchQuery = ""
        searchResults = emptyList()
        errorMessage = null
    }
}
//package com.afsar.titipin.ui.circle.add
//
//import com.afsar.titipin.data.model.User
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.setValue
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.afsar.titipin.data.remote.AuthRepository
//import com.afsar.titipin.data.remote.repository.circle.CircleRepository
//import dagger.hilt.android.lifecycle.HiltViewModel
//import kotlinx.coroutines.Job
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.launch
//import javax.inject.Inject
//
//@HiltViewModel
//class AddCircleViewModel @Inject constructor(
//    private val authRepository: AuthRepository,
//    private val circleRepository: CircleRepository
//) : ViewModel() {
//
//    var newCircleName by mutableStateOf("")
//    var searchQuery by mutableStateOf("")
//    var searchResults by mutableStateOf<List<User>>(emptyList())
//    var selectedMembers by mutableStateOf<List<User>>(emptyList())
//    var isCreating by mutableStateOf(false)
//    var isLoading by mutableStateOf(false)
//    var errorMessage by mutableStateOf<String?>(null)
//    private var searchJob: Job? = null
//
//    fun onSearchQueryChange(query: String) {
//        searchQuery = query
//        searchJob?.cancel()
//        searchJob = viewModelScope.launch {
//            delay(500)
//            if (query.isNotEmpty()) {
//                authRepository.searchUsers(query).collect { result ->
//                    result.onSuccess { searchResults = it }
//                }
//            } else {
//                searchResults = emptyList()
//            }
//        }
//    }
//
//    fun addMemberToSelection(user: User) {
//        if (!selectedMembers.contains(user)) {
//            selectedMembers = selectedMembers + user
//        }
//        searchQuery = ""
//        searchResults = emptyList()
//    }
//    fun removeMemberFromSelection(user: User) {
//        selectedMembers = selectedMembers - user
//    }
//
//    fun createCircle(onSuccess: () -> Unit) {
//        if (newCircleName.isBlank()) return
//
//        isCreating = true
//        viewModelScope.launch {
//            circleRepository.createCircle(newCircleName, selectedMembers).collect {
//                isCreating = false
//                onSuccess()
//                newCircleName = ""
//                selectedMembers = emptyList()
//            }
//        }
//    }
//
////    var newCircleName by mutableStateOf("")
////    var searchQuery by mutableStateOf("")
////    var searchResults by mutableStateOf<List<User>>(emptyList())
////    var selectedMembers by mutableStateOf<List<User>>(emptyList())
////    var isCreating by mutableStateOf(false)
////    var isLoading by mutableStateOf(false)
////    var errorMessage by mutableStateOf<String?>(null)
////
////    private var searchJob: Job? = null
////
////    fun onSearchQueryChange(query: String) {
////        searchQuery = query
////        searchJob?.cancel()
////        searchJob = viewModelScope.launch {
////            delay(500)
////            if (query.isNotEmpty()) {
////                repository.searchUsers(query).collect { result ->
////                    result.onSuccess { searchResults = it }
////                }
////            } else {
////                searchResults = emptyList()
////            }
////        }
////    }
////
////    fun addMemberToSelection(user: User) {
////        if (!selectedMembers.contains(user)) {
////            selectedMembers = selectedMembers + user
////        }
////        searchQuery = ""
////        searchResults = emptyList()
////    }
////
////    fun removeMemberFromSelection(user: User) {
////        selectedMembers = selectedMembers - user
////    }
////
////    fun createCircle(onSuccess: () -> Unit) {
////        if (newCircleName.isBlank()) return
////
////        isCreating = true
////        viewModelScope.launch {
////            repository.createCircle(newCircleName, selectedMembers).collect {
////                isCreating = false
////                onSuccess()
////                newCircleName = ""
////                selectedMembers = emptyList()
////            }
////        }
////    }
//
//}