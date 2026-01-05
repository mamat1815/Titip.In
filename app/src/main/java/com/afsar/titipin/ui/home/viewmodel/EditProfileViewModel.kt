package com.afsar.titipin.ui.home.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afsar.titipin.data.remote.AuthRepository
import com.afsar.titipin.data.remote.profile.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    // Form State
    var name by mutableStateOf("")
    var username by mutableStateOf("")
    var phone by mutableStateOf("")

    var isLoading by mutableStateOf(false)
    var isSuccess by mutableStateOf(false)

    init {
        // Load data awal
        viewModelScope.launch {
            authRepository.getUserProfile().collect {
                it.onSuccess { user ->
                    name = user.name
                    username = user.username
                    phone = user.phoneNumber
                }
            }
//            }
//            if (user != null) {
//                name = user.collect { name }.toString()
//                username = user.collect { username }.toString()
//                phone = user.collect { phone }.toString()
//            }
        }
    }

    fun saveProfile() {
        isLoading = true
        viewModelScope.launch {
            profileRepository.updateProfile(name, username, phone).collect {
                isLoading = false
                if (it.isSuccess) isSuccess = true
            }
        }
    }
}