package com.afsar.titipin.ui.session
import android.content.Context
import android.location.Geocoder
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afsar.titipin.data.model.Circle
import com.afsar.titipin.data.model.JastipSession
import com.afsar.titipin.data.remote.AuthRepository
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
    private val repository: AuthRepository
) : ViewModel() {

    // ... (State title, description tetap sama) ...
    var title by mutableStateOf("")
    var description by mutableStateOf("")
    var selectedLatLng by mutableStateOf<LatLng?>(null)

    // UPDATE: Location bukan cuma String, tapi kita simpan koordinat juga
    var locationName by mutableStateOf("") // Nama Jalan/Gedung
    var locationLatLng by mutableStateOf<LatLng?>(null) // Koordinat Map

    var selectedDuration by mutableIntStateOf(5)
    var maxTitip by mutableIntStateOf(5)

    var myCircles by mutableStateOf<List<Circle>>(emptyList())
    var selectedCircle by mutableStateOf<Circle?>(null)
    var searchQuery by mutableStateOf("")

    var isLoading by mutableStateOf(false)
    var isSuccess by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    init {
        loadCircles()
    }

    private fun loadCircles() {
        viewModelScope.launch {
            repository.getMyCircles().collect { result ->
                result.onSuccess { myCircles = it }
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
                    errorMessage = "Lokasi tidak ditemukan, coba buka Google Maps dulu."
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

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)

                withContext(Dispatchers.Main) {
                    isLoading = false
                    if (!addresses.isNullOrEmpty()) {
                        val address = addresses[0]

                        val feature = address.featureName
                        val througfare = address.thoroughfare
                        val locality = address.subLocality ?: address.locality


                        val fullAddress = listOfNotNull(feature, througfare, locality).joinToString(", ")
                        locationName = fullAddress
                    } else {

                        locationName = "${latLng.latitude}, ${latLng.longitude}"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isLoading = false

                    locationName = "Lokasi Terpilih (${latLng.latitude}, ${latLng.longitude})"
                }
            }
        }
    }
    fun createSession() {

        if (title.isBlank() || locationName.isBlank() || selectedCircle == null) {
            errorMessage = "Mohon lengkapi data form."
            return
        }

        if (selectedCircle!!.isActiveSession) {
            errorMessage = "Gagal: Circle '${selectedCircle!!.name}' masih memiliki sesi aktif!"
            return
        }

        isLoading = true
        errorMessage = null

        viewModelScope.launch {
            repository.getUserProfile().collect { userResult ->
                val creatorName = userResult.getOrNull()?.name ?: "Unknown"


                val finalLat = selectedLatLng?.latitude ?: 0.0
                val finalLng = selectedLatLng?.longitude ?: 0.0

                val newSession = JastipSession(
                    title = title,
                    description = description,
                    locationName = locationName,
                    latitude = finalLat,
                    longitude = finalLng,
                    durationMinutes = selectedDuration,
                    maxTitip = maxTitip,
                    circleId = selectedCircle!!.id,
                    circleName = selectedCircle!!.name,
                    creatorName = creatorName
                )

                repository.createJastipSession(newSession).collect { result ->
                    isLoading = false
                    result.onSuccess { isSuccess = true }
                    result.onFailure { errorMessage = it.localizedMessage }
                }
            }
        }
    }
}