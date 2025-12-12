package com.afsar.titipin.ui.home.viewmodel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afsar.titipin.data.model.Circle
import com.afsar.titipin.data.model.JastipSession
import com.afsar.titipin.data.model.User
import com.afsar.titipin.data.remote.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    var currentUser by mutableStateOf<User?>(null)
        private set

    var myCircles by mutableStateOf<List<Circle>>(emptyList())
        private set

    var mySessions by mutableStateOf<List<JastipSession>>(emptyList())
        private set

    var isLoading by mutableStateOf(true)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        fetchUserProfile()
        loadMyCircles()
        loadMySessions()
    }

    fun fetchUserProfile() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            repository.getUserProfile().collect { result ->
                isLoading = false
                result.onSuccess { user ->
                    currentUser = user
                }
                result.onFailure { error ->
                    errorMessage = error.localizedMessage ?: "Gagal memuat profil"
                }
            }
        }
    }

    fun loadMyCircles() {
        viewModelScope.launch {
            repository.getMyCircles().collect { result ->
                result.onSuccess { circles ->
                    myCircles = circles
                }
            }
        }
    }

    fun loadMySessions() {
        viewModelScope.launch {
            repository.getMyJastipSessions().collect { result ->
                result.onSuccess { sessions ->
                    mySessions = sessions
                }
            }
        }
    }

}