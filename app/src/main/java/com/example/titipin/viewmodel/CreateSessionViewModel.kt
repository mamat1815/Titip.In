package com.example.titipin.viewmodel

import androidx.lifecycle.ViewModel
import com.example.titipin.model.CircleGroup
import com.example.titipin.model.TitipanCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CreateSessionViewModel : ViewModel() {

    // State untuk Form Input
    var title = MutableStateFlow("")
    var description = MutableStateFlow("")
    var location = MutableStateFlow("")
    
    // State untuk Kategori Titipan
    var selectedCategory = MutableStateFlow(TitipanCategory.MAKANAN_MINUMAN)

    // State untuk Pilihan Durasi (dalam menit)
    private val _selectedDuration = MutableStateFlow(5)
    val selectedDuration = _selectedDuration.asStateFlow()

    // State untuk Counter (Maksimal Penitip)
    private val _maxPeople = MutableStateFlow(5)
    val maxPeople = _maxPeople.asStateFlow()

    // State untuk List Circle
    private val _circleList = MutableStateFlow<List<CircleGroup>>(emptyList())
    val circleList = _circleList.asStateFlow()

    init {
        loadDummyCircles()
    }

    private fun loadDummyCircles() {
        _circleList.value = listOf(
            CircleGroup(1, "Teman Kantor", 12, "https://thefutureispublictransport.org/wp-content/uploads/2022/09/Story-C40-SiteImage_04.jpg"),
            CircleGroup(2, "Anak Fasilkom", 89, "https://ui-avatars.com/api/?name=AF&background=random", isSelected = true),
            CircleGroup(3, "Mabar Valorant", 5, "https://ui-avatars.com/api/?name=MV&background=random")
        )
    }

    fun setDuration(minutes: Int) {
        _selectedDuration.value = minutes
    }

    fun incrementPeople() {
        _maxPeople.value += 1
    }

    fun decrementPeople() {
        if (_maxPeople.value > 1) _maxPeople.value -= 1
    }

    fun toggleCircleSelection(id: Int) {
        _circleList.value = _circleList.value.map {
            if (it.id == id) it.copy(isSelected = !it.isSelected) else it
        }
    }
}