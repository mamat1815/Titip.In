package com.afsar.titipin.ui.session.add

import android.content.Context
import android.location.Geocoder
import android.annotation.SuppressLint
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afsar.titipin.data.model.Category
import com.afsar.titipin.data.model.Circle
import com.afsar.titipin.data.model.Session
import com.afsar.titipin.data.model.User
import com.afsar.titipin.data.remote.AuthRepository
import com.afsar.titipin.data.remote.repository.circle.CircleRepository
import com.afsar.titipin.data.remote.repository.session.SessionRepository
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class CreateSessionViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val circleRepository: CircleRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    // --- INPUT FORM ---
    var title by mutableStateOf("")
    var locationName by mutableStateOf("")
    var description by mutableStateOf("")

    // --- LOCATION DATA ---
    var selectedLatLng by mutableStateOf<LatLng?>(null)

    // --- SETTINGS ---
    var selectedDuration by mutableFloatStateOf(15f)
    var maxTitip by mutableFloatStateOf(5f)

    // --- CATEGORY ---
    var category by mutableStateOf(Category.FOOD)

    // --- CIRCLE SELECTION ---
    var myCircles by mutableStateOf<List<Circle>>(emptyList())
    var selectedCircle by mutableStateOf<Circle?>(null)
    var searchQuery by mutableStateOf("")

    // --- USER DATA ---
    private var currentUser: User? = null

    // --- UI STATE ---
    var isLoading by mutableStateOf(false)
    var isSuccess by mutableStateOf(false) // Trigger navigasi balik
    var errorMessage by mutableStateOf<String?>(null)

    init {
        loadData()
    }

    private fun loadData() {
        // 1. Ambil Data Circle Saya
        viewModelScope.launch {
            circleRepository.getMyCircles().collect { result ->
                result.onSuccess {
                    myCircles = it
                    // HAPUS BARIS INI: isSuccess = true
                    // isSuccess hanya boleh true kalau CREATE SESSION berhasil,
                    // bukan saat load data awal.
                }
                result.onFailure {
                    errorMessage = "Gagal memuat circle: ${it.message}"
                }
            }
        }

        // 2. Ambil Data User
        viewModelScope.launch {
            authRepository.getUserProfile().collect { result ->
                result.onSuccess { currentUser = it }
            }
        }
    }

    // Filter Circle
    fun getFilteredCircles(): List<Circle> {
        return if (searchQuery.isBlank()) myCircles
        else myCircles.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    // --- LOCATION LOGIC (DIPERBAIKI AGAR LEBIH KUAT) ---
    @SuppressLint("MissingPermission") // Izin sudah dicek di UI
    fun fetchCurrentLocation(context: Context) {
        isLoading = true
        errorMessage = null
        try {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

            // FIX: Gunakan getCurrentLocation dengan Prioritas Tinggi
            // Ini memaksa HP/Emulator mencari satelit saat itu juga, bukan cuma ambil cache lama
            val cancellationTokenSource = CancellationTokenSource()

            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).addOnSuccessListener { location ->
                if (location != null) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    updateLocationFromMap(latLng, context)
                } else {
                    isLoading = false
                    errorMessage = "Lokasi belum terdeteksi. Coba geser peta manual."
                }
            }.addOnFailureListener {
                isLoading = false
                errorMessage = "Gagal mengambil lokasi: ${it.message}"
            }
        } catch (e: Exception) {
            isLoading = false
            errorMessage = "Error lokasi: ${e.localizedMessage}"
        }
    }

    fun updateLocationFromMap(latLng: LatLng, context: Context) {
        selectedLatLng = latLng
        isLoading = true
        errorMessage = null

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)

                withContext(Dispatchers.Main) {
                    isLoading = false
                    if (!addresses.isNullOrEmpty()) {
                        val address = addresses[0]
                        val street = address.thoroughfare ?: address.featureName
                        val area = address.subLocality ?: address.locality
                        locationName = listOfNotNull(street, area).joinToString(", ")
                    } else {
                        locationName = String.format("%.5f, %.5f", latLng.latitude, latLng.longitude)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isLoading = false
                    locationName = String.format("%.5f, %.5f", latLng.latitude, latLng.longitude)
                }
            }
        }
    }

    // --- CREATE SESSION ACTION ---
    fun createSession() {
        // Validasi
        if (title.isBlank()) { errorMessage = "Isi judul titipan dulu!"; return }
        if (selectedLatLng == null) { errorMessage = "Lokasi belum dipilih!"; return }
        if (selectedCircle == null) { errorMessage = "Pilih circle tujuan!"; return }

        // Cek User
        val user = currentUser
        if (user == null) {
            errorMessage = "Profil user belum dimuat. Tunggu sebentar..."
            loadData()
            return
        }

        // Cek Sesi Aktif di Circle
        if (selectedCircle!!.isActiveSession) {
            errorMessage = "Circle ini sedang ada sesi aktif. Tunggu selesai dulu ya."
            return
        }

        isLoading = true
        errorMessage = null

        viewModelScope.launch {
            val newSession = Session(
                title = title,
                description = description,
                category = category,

                locationName = locationName,
                latitude = selectedLatLng!!.latitude,
                longitude = selectedLatLng!!.longitude,

                durationMinutes = selectedDuration.roundToInt(),
                maxTitip = maxTitip.roundToInt(),

                circleId = selectedCircle!!.id,
                circleName = selectedCircle!!.name,
                creatorId = user.uid,
                creatorName = user.name,

                status = "open",
                currentTitipCount = 0
            )

            sessionRepository.createSession(newSession).collect { result ->
                isLoading = false
                result.onSuccess {
                    // NAH, DI SINI BARU BENAR SET SUCCESS
                    isSuccess = true
                }
                result.onFailure {
                    errorMessage = it.localizedMessage ?: "Gagal membuat sesi."
                }
            }
        }
    }
}