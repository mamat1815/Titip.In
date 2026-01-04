package com.afsar.titipin.ui.session.add

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Dehaze
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.afsar.titipin.R
import com.afsar.titipin.data.model.Category
import com.afsar.titipin.data.model.Circle
import com.afsar.titipin.ui.components.molecules.SessionProgressBar
import com.afsar.titipin.ui.session.LocationPickerDialog
import com.afsar.titipin.ui.theme.OrangePrimary
import com.afsar.titipin.ui.theme.OrangeSecondary
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSessionScreens(
    onBackClick: () -> Unit,
    viewModel: CreateSessionViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var showMapDialog by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (viewModel.selectedLatLng == null) {
                viewModel.fetchCurrentLocation(context)
            }
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (isGranted) {
            viewModel.fetchCurrentLocation(context)
        }
    }

    if (showMapDialog) {
        LocationPickerDialog(
            initialLocation = viewModel.selectedLatLng,
            onDismiss = { showMapDialog = false },
            onLocationSelected = { latLng ->
                viewModel.updateLocationFromMap(latLng, context)
                showMapDialog = false
            }
        )
    }

    LaunchedEffect(viewModel.isSuccess) {
        if (viewModel.isSuccess) {
            onBackClick()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Buat Sesi Baru", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF9FAFB))
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Button(
                    onClick = { viewModel.createSession() },
                    enabled = !viewModel.isLoading && viewModel.locationName.isNotEmpty() && viewModel.selectedCircle != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(100),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OrangePrimary,
                        contentColor = Color.White,
                        disabledContainerColor = OrangeSecondary
                    )
                ) {
                    if (viewModel.isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Buat Sesi Sekarang", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        },
        containerColor = Color(0xFFF9FAFB)
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {


            Spacer(modifier = Modifier.height(16.dp))

            SessionProgressBar(
                currentStep = 1,
                instructionText = "Lengkapi detail sesi titipanmu agar temanmu bisa mulai menitip!",
                iconRes = R.drawable.ic_sesi,
                modifier = Modifier.padding(
                    bottom = 16.dp,
                    start = 16.dp,
                    end = 16.dp
                    )
            )

            if (viewModel.errorMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Text(
                        text = viewModel.errorMessage!!,
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            MapSection(
                locationName = viewModel.locationName,
                onLocationNameChange = { viewModel.locationName = it },
                title = viewModel.title,
                onTitleChange = { viewModel.title = it },
                description = viewModel.description,
                onDescriptionChange = { viewModel.description = it },

                currentLatLng = viewModel.selectedLatLng,

                onMyLocationClick = {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        viewModel.fetchCurrentLocation(context)
                    } else {
                        locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                    }
                },
                onMapAreaClick = { showMapDialog = true },
                isLocationLoading = viewModel.isLoading
            )

            Spacer(modifier = Modifier.height(12.dp))
            CategorySelectorSection(
                selectedCategory = viewModel.category,
                onCategorySelected = { viewModel.category = it }
            )

            Divider(thickness = 8.dp, color = Color(0xFFF0F0F0), modifier = Modifier.padding(vertical = 16.dp))

            SettingSection(
                duration = viewModel.selectedDuration,
                onDurationChange = { viewModel.selectedDuration = it },
                maxTitip = viewModel.maxTitip.toFloat(),
                onMaxTitipChange = { viewModel.maxTitip = it }
            )

            Divider(thickness = 8.dp, color = Color(0xFFF0F0F0))

            CircleSelectorSectionNew(
                circles = viewModel.getFilteredCircles(),
                searchQuery = viewModel.searchQuery,
                onSearchQueryChange = { viewModel.searchQuery = it },
                selectedCircleId = viewModel.selectedCircle?.id,
                onCircleSelected = { circle -> viewModel.selectedCircle = circle }
            )

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun MapSection(
    locationName: String,
    onLocationNameChange: (String) -> Unit,
    title: String,
    onTitleChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    currentLatLng: LatLng?,
    onMyLocationClick: () -> Unit,
    onMapAreaClick: () -> Unit,
    isLocationLoading: Boolean
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Detail Lokasi & Sesi", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            placeholder = { Text("Mau titip apa? (Contoh: Beli Makan Siang)") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = OrangePrimary,
                unfocusedBorderColor = Color.LightGray,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )

        Surface(
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 0.dp, bottomEnd = 0.dp),
            border = BorderStroke(1.dp, Color.LightGray),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                value = locationName,
                onValueChange = onLocationNameChange,
                placeholder = { Text("Lokasi tujuan...") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(20.dp))
                },
                trailingIcon = {
                    IconButton(onClick = onMyLocationClick) {
                        if (isLocationLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(24.dp))
                        }
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .offset(y = (-1).dp)
                .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                .border(1.dp, Color.LightGray, RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
        ) {
            if (currentLatLng != null) {
                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(currentLatLng, 15f)
                }

                LaunchedEffect(currentLatLng) {
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(currentLatLng, 15f)
                }

                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = false,
                        scrollGesturesEnabled = false,
                        zoomGesturesEnabled = false,
                        rotationGesturesEnabled = false,
                        tiltGesturesEnabled = false
                    )
                ) {
                    val markerState = rememberMarkerState(position = currentLatLng)

                    LaunchedEffect(currentLatLng) {
                        markerState.position = currentLatLng
                    }
                    Marker(
                        state = markerState,
                        title = "Lokasi Terpilih"
                    )
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color(0xFFEEEEEE)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(32.dp))
                        Text("Belum ada lokasi dipilih", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onMapAreaClick() }
            ) {
                Surface(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(8.dp),
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = "Ketuk peta untuk ubah lokasi",
                        color = Color.White,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            placeholder = { Text("Catatan tambahan (Opsional)") },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Dehaze, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(20.dp))
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = OrangePrimary,
                unfocusedBorderColor = Color.LightGray,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )
    }
}

@Composable
fun CategorySelectorSection(
    selectedCategory: Category,
    onCategorySelected: (Category) -> Unit
) {
    fun getIcon(cat: Category): Int {
        return when(cat) {
            Category.FOOD -> R.drawable.ic_makanan
            Category.MEDICINE -> R.drawable.ic_obat
            Category.SHOPPING -> R.drawable.ic_belanja
        }
    }

    fun getName(cat: Category): String {
        return when(cat) {
            Category.FOOD -> "Makanan"
            Category.MEDICINE -> "Obat-obatan"
            Category.SHOPPING -> "Belanjaan"
        }
    }

    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text("Kategori", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color.LightGray),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clickable { expanded = true }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = getIcon(selectedCategory)),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = getName(selectedCategory), fontSize = 14.sp, color = Color.Black)
                }
                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.Gray)
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White).fillMaxWidth(0.9f)
        ) {
            Category.values().forEach { category ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = getIcon(category)),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = getName(category))
                        }
                    },
                    onClick = {
                        onCategorySelected(category)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun SettingSection(
    duration: Float,
    onDurationChange: (Float) -> Unit,
    maxTitip: Float,
    onMaxTitipChange: (Float) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("Durasi Menunggu", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(R.drawable.ic_timer),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "${duration.roundToInt()} menit", fontWeight = FontWeight.Bold)
            }
        }
        Slider(
            value = duration,
            onValueChange = onDurationChange,
            valueRange = 5f..60f,
            steps = 10,
            colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = OrangePrimary)
        )
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("5 m", fontSize = 10.sp, color = Color.Gray)
            Text("60 m", fontSize = 10.sp, color = Color.Gray)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("Maksimal Penitip", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Group, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "${maxTitip.roundToInt()} orang", fontWeight = FontWeight.Bold)
            }
        }
        Slider(
            value = maxTitip,
            onValueChange = onMaxTitipChange,
            valueRange = 1f..10f,
            steps = 8,
            colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = OrangePrimary)
        )
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("1", fontSize = 10.sp, color = Color.Gray)
            Text("10", fontSize = 10.sp, color = Color.Gray)
        }
    }
}

@Composable
fun CircleSelectorSectionNew(
    circles: List<Circle>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedCircleId: String?,
    onCircleSelected: (Circle) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Bagikan Ke Circle", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("Cari Circle..") },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(20.dp))
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = Color.LightGray,
                unfocusedBorderColor = Color.LightGray
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (circles.isEmpty()) {
            Text("Tidak ada circle ditemukan.", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(8.dp))
        }

        circles.forEach { circle ->
            val isSelected = selectedCircleId == circle.id

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onCircleSelected(circle) },
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) OrangePrimary.copy(alpha = 0.1f) else Color.White,
                border = BorderStroke(1.dp, if (isSelected) OrangePrimary else Color.Transparent)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFFFF3E0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Group, contentDescription = null, tint = Color(0xFF795548))
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(circle.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text("${circle.memberIds.size} anggota", fontSize = 12.sp, color = Color.Gray)
                    }

                    Icon(
                        imageVector = if (isSelected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (isSelected) OrangePrimary else Color.LightGray
                    )
                }
            }
        }
    }
}