package com.afsar.titipin.ui.session.add

import android.content.Context
import android.location.Geocoder
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afsar.titipin.data.model.Circle
import com.afsar.titipin.data.model.Session
import com.afsar.titipin.data.model.User
import com.afsar.titipin.data.remote.AuthRepository
import com.afsar.titipin.data.remote.repository.circle.CircleRepository
import com.afsar.titipin.data.remote.repository.session.SessionRepository
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class CreateSessionViewModel @Inject constructor(
    private val sessionRepository: SessionRepository, // Ganti ke Repo yang benar
    private val circleRepository: CircleRepository,   // Ganti ke Repo yang benar
    private val authRepository: AuthRepository        // Untuk ambil data diri sendiri
) : ViewModel() {

    // Input Form
    var title by mutableStateOf("")
    var description by mutableStateOf("")

    // Location
    var selectedLatLng by mutableStateOf<LatLng?>(null)
    var locationName by mutableStateOf("")

    // Settings
    var selectedDuration by mutableIntStateOf(15) // Default 15 menit
    var maxTitip by mutableIntStateOf(5)

    // Circle Selection
    var myCircles by mutableStateOf<List<Circle>>(emptyList())
    var selectedCircle by mutableStateOf<Circle?>(null)
    var searchQuery by mutableStateOf("")

    // User Data
    private var currentUser: User? = null

    // UI State
    var isLoading by mutableStateOf(false)
    var isSuccess by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    init {
        loadData()
    }

    private fun loadData() {
        // 1. Ambil Data Circle
        viewModelScope.launch {
            circleRepository.getMyCircles().collect { result ->
                result.onSuccess { myCircles = it }
            }
        }

        // 2. Ambil Data User (untuk creatorId & creatorName)
        viewModelScope.launch {
            authRepository.getUserProfile().collect { result ->
                result.onSuccess { currentUser = it }
            }
        }
    }

    fun getFilteredCircles(): List<Circle> {
        return if (searchQuery.isBlank()) myCircles
        else myCircles.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    fun incrementMaxTitip() { maxTitip++ }
    fun decrementMaxTitip() { if (maxTitip > 1) maxTitip-- }

    fun fetchCurrentLocation(context: Context) {
        isLoading = true
        try {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    updateLocationFromMap(latLng, context)
                } else {
                    isLoading = false
                    errorMessage = "Lokasi tidak ditemukan, pastikan GPS aktif."
                }
            }.addOnFailureListener {
                isLoading = false
                errorMessage = "Gagal mengambil lokasi: ${it.message}"
            }
        } catch (e: SecurityException) {
            isLoading = false
            errorMessage = "Izin lokasi belum diberikan"
        } catch (e: Exception) {
            isLoading = false
            errorMessage = "Error lokasi: ${e.localizedMessage}"
        }
    }

    fun updateLocationFromMap(latLng: LatLng, context: Context) {
        selectedLatLng = latLng
        isLoading = true
        errorMessage = null

        viewModelScope.launch(Dispatchers.IO) { // Pindah ke IO Thread agar UI tidak macet
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION") // Untuk kompatibilitas API lama
                val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)

                withContext(Dispatchers.Main) {
                    isLoading = false
                    if (!addresses.isNullOrEmpty()) {
                        val address = addresses[0]

                        // Logika nama jalan yang lebih rapi
                        val street = address.thoroughfare ?: address.featureName
                        val area = address.subLocality ?: address.locality

                        locationName = listOfNotNull(street, area).joinToString(", ")
                    } else {
                        locationName = "${latLng.latitude}, ${latLng.longitude}"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isLoading = false
                    // Fallback jika geocoder gagal (misal tidak ada internet)
                    locationName = String.format("%.5f, %.5f", latLng.latitude, latLng.longitude)
                }
            }
        }
    }

    fun createSession() {
        // Validasi Input
        if (title.isBlank()) {
            errorMessage = "Judul sesi tidak boleh kosong."
            return
        }
        if (selectedLatLng == null || locationName.isBlank()) {
            errorMessage = "Silakan pilih lokasi tujuan."
            return
        }
        if (selectedCircle == null) {
            errorMessage = "Pilih circle tujuan share."
            return
        }
        if (currentUser == null) {
            errorMessage = "Data user belum termuat. Coba lagi."
            // Retry fetch user
            loadData()
            return
        }

        // Validasi Sesi Aktif di Circle
        if (selectedCircle!!.isActiveSession) {
            errorMessage = "Gagal: Circle '${selectedCircle!!.name}' masih memiliki sesi aktif!"
            return
        }

        isLoading = true
        errorMessage = null

        viewModelScope.launch {
            val newSession = Session(
                title = title,
                description = description,
                locationName = locationName,
                latitude = selectedLatLng!!.latitude,
                longitude = selectedLatLng!!.longitude,
                durationMinutes = selectedDuration,
                maxTitip = maxTitip,
                circleId = selectedCircle!!.id,
                circleName = selectedCircle!!.name,
                creatorId = currentUser!!.uid,
                creatorName = currentUser!!.name,
                // createdAt diisi server
            )

            sessionRepository.createSession(newSession).collect { result ->
                isLoading = false
                result.onSuccess { isSuccess = true }
                result.onFailure { errorMessage = it.localizedMessage ?: "Gagal membuat sesi" }
            }
        }
    }
}