package com.afsar.titipin.ui.session
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.afsar.titipin.data.model.Circle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSessionScreen(
    onBackClick: () -> Unit,
    viewModel: CreateSessionViewModel = hiltViewModel()
) {

    val context = LocalContext.current
    var showMapDialog by remember { mutableStateOf(false) }
    LaunchedEffect(viewModel.isSuccess) {
        if (viewModel.isSuccess) {
            onBackClick()
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
            onDismiss = { showMapDialog = false },
            onLocationSelected = { latLng ->
                viewModel.updateLocationFromMap(latLng, context)
                showMapDialog = false
            }
        )
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Buat Sesi Titipan Baru", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF5F7FA))
            )
        },
        bottomBar = {

            Button(
                onClick = { viewModel.createSession() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF370061)),
                enabled = !viewModel.isLoading
            ) {
                if (viewModel.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Buat Sesi Titipan", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        },
        containerColor = Color(0xFFF5F7FA)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {

            if (viewModel.errorMessage != null) {
                Text(viewModel.errorMessage!!, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
            }


            InputLabel("Judul Sesi")
            OutlinedTextField(
                value = viewModel.title,
                onValueChange = { viewModel.title = it },
                placeholder = { Text("Mau titip apa?") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = Color(0xFF370061)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))


            InputLabel("Deskripsi Singkat (Opsional)")
            OutlinedTextField(
                value = viewModel.description,
                onValueChange = { viewModel.description = it },
                placeholder = { Text("Tulis catatan tambahan di sini...") },
                modifier = Modifier.fillMaxWidth().height(100.dp),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            InputLabel("Lokasi/Tujuan")
            OutlinedTextField(
                value = viewModel.locationName,
                onValueChange = { viewModel.locationName = it },
                placeholder = { Text("Pilih lokasi dari peta...") },
                trailingIcon = {
                    Row {

                        IconButton(onClick = {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                viewModel.fetchCurrentLocation(context)
                            } else {
                                locationPermissionLauncher.launch(arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                ))
                            }
                        }) {
                            Icon(Icons.Default.MyLocation, contentDescription = "Current Location", tint = Color(0xFF370061))
                        }

                        IconButton(onClick = { showMapDialog = true }) {
                            Icon(Icons.Default.LocationOn, contentDescription = "Open Map", tint = Color.Gray)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),

                // readOnly = false
            )
            Spacer(modifier = Modifier.height(16.dp))

            InputLabel("Durasi Sesi")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(5, 10, 15).forEach { mins ->
                    DurationChip(
                        minutes = mins,
                        isSelected = viewModel.selectedDuration == mins,
                        onClick = { viewModel.selectedDuration = mins }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            InputLabel("Maksimal Penitip")
            CounterInput(
                value = viewModel.maxTitip,
                onIncrement = { viewModel.incrementMaxTitip() },
                onDecrement = { viewModel.decrementMaxTitip() }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text("Bagikan Sesi Ke Circle", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    OutlinedTextField(
                        value = viewModel.searchQuery,
                        onValueChange = { viewModel.searchQuery = it },
                        placeholder = { Text("Cari circle...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF5F7FA),
                            unfocusedContainerColor = Color(0xFFF5F7FA),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val circles = viewModel.getFilteredCircles()
                    if (circles.isEmpty()) {
                        Text("Tidak ada circle ditemukan.", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(8.dp))
                    } else {
                        circles.forEach { circle ->
                            CircleSelectionItem(
                                circle = circle,
                                isSelected = viewModel.selectedCircle?.id == circle.id,
                                onClick = { viewModel.selectedCircle = circle }
                            )
                            if (circle != circles.last()) {
                                Divider(color = Color.LightGray.copy(alpha = 0.2f), thickness = 0.5.dp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- SUB-COMPONENTS ---

@Composable
fun InputLabel(text: String) {
    Text(
        text = text,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        color = Color(0xFF1E1E1E),
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun DurationChip(minutes: Int, isSelected: Boolean, onClick: () -> Unit) {
    val bgColor = if (isSelected) Color(0xFFF2E7FE) else Color.White
    val borderColor = if (isSelected) Color(0xFF370061) else Color.LightGray
    val textColor = if (isSelected) Color(0xFF370061) else Color.Gray

    Box(
        modifier = Modifier
            .width(80.dp)
            .height(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text("$minutes mnt", color = textColor, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun CounterInput(value: Int, onIncrement: () -> Unit, onDecrement: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Tombol Minus
        IconButton(onClick = onDecrement, modifier = Modifier.background(Color(0xFFF5F5F5), RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))) {
            Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = Color.Gray)
        }

        Text(text = value.toString(), fontWeight = FontWeight.Bold, fontSize = 16.sp)

        // Tombol Plus
        IconButton(onClick = onIncrement, modifier = Modifier.background(Color(0xFFF5F5F5), RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp))) {
            Icon(Icons.Default.Add, contentDescription = "Increase", tint = Color.Gray)
        }
    }
}

@Composable
fun CircleSelectionItem(circle: Circle, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Radio Button (Custom Look)
        Icon(
            imageVector = if (isSelected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (isSelected) Color(0xFF370061) else Color.LightGray,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Avatar Circle
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFF3E0)), // Warna krem seperti di desain
            contentAlignment = Alignment.Center
        ) {
            // Bisa ganti AsyncImage jika circle punya foto
            Icon(Icons.Default.Group, contentDescription = null, tint = Color(0xFF795548))
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Info Circle
        Column {
            Text(circle.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text("${circle.members.size} anggota", fontSize = 12.sp, color = Color.Gray)
        }
    }
}