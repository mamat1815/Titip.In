package com.example.titipin.ui.screen

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.titipin.R
import com.example.titipin.model.CircleGroup
import com.example.titipin.model.TitipanCategory
import com.example.titipin.ui.theme.*
import com.example.titipin.viewmodel.CreateSessionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSessionScreen(
    viewModel: CreateSessionViewModel = viewModel(),
    onBackClick: () -> Unit,
    onSessionCreated: () -> Unit = {}
) {
    // Collect State dari ViewModel
    val title by viewModel.title.collectAsState()
    val description by viewModel.description.collectAsState()
    val location by viewModel.location.collectAsState()
    val duration by viewModel.selectedDuration.collectAsState()
    val maxPeople by viewModel.maxPeople.collectAsState()
    val circles by viewModel.circleList.collectAsState()

    Scaffold(
        topBar = {
            // Header Custom sesuai desain
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BgLight.copy(alpha = 0.9f))
                    .statusBarsPadding()
                    .height(64.dp)
                    .padding(horizontal = 16.dp)
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Text(
                    text = "Buat Sesi Titipan Baru",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        },
        bottomBar = {
            // Tombol Submit melayang di bawah
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .navigationBarsPadding() // Agar tidak tertutup gesture bar HP
            ) {
                Button(
                    onClick = onSessionCreated,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(8.dp, RoundedCornerShape(12.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.AddCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Buat Sesi Titipan", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        containerColor = BgLight
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // 1. Input Judul
            InputLabel("Judul Sesi")
            CustomTextField(
                value = title,
                onValueChange = { viewModel.title.value = it },
                placeholder = "Mau titip apa?"
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Input Deskripsi
            InputLabel("Deskripsi Singkat (Opsional)")
            CustomTextField(
                value = description,
                onValueChange = { viewModel.description.value = it },
                placeholder = "Tulis catatan tambahan di sini...",
                isSingleLine = false,
                minLines = 3
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Pilihan Kategori
            InputLabel("Kategori Titipan")
            CategorySelector(
                selectedCategory = viewModel.selectedCategory.collectAsState().value,
                onCategorySelected = { viewModel.selectedCategory.value = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 4. Pilihan Lokasi dengan Map Preview
            InputLabel("Lokasi/Tujuan")
            
            var showLocationDialog by remember { mutableStateOf(false) }
            
            LocationPickerCard(
                selectedLocation = location,
                onClick = { showLocationDialog = true }
            )
            
            // Location Selection Dialog
            if (showLocationDialog) {
                LocationSelectionDialog(
                    onLocationSelected = { 
                        viewModel.location.value = it
                        showLocationDialog = false
                    },
                    onDismiss = { showLocationDialog = false }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4. Pilihan Durasi (Slider)
            InputLabel("Durasi Sesi")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$duration menit",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    modifier = Modifier.width(80.dp)
                )
                
                Slider(
                    value = duration.toFloat(),
                    onValueChange = { viewModel.setDuration(it.toInt()) },
                    valueRange = 1f..15f,
                    steps = 13,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = PrimaryColor,
                        activeTrackColor = Color(0xFFE0E0E0),
                        inactiveTrackColor = Color(0xFFE0E0E0)
                    ),
                    thumb = {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(PrimaryColor, CircleShape)
                        )
                    },
                    track = { sliderState ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(28.dp)
                                .background(Color(0xFFE0E0E0), RoundedCornerShape(16.dp))
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 5. Counter Maksimal Penitip
            InputLabel("Maksimal Penitip")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { viewModel.decrementPeople() }) {
                    Icon(Icons.Default.Remove, contentDescription = null, tint = TextSecondary)
                }
                Text(maxPeople.toString(), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                IconButton(onClick = { viewModel.incrementPeople() }) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 6. List Circle (Bagikan Ke)
            InputLabel("Bagikan Sesi Ke Circle")
            // Search Bar Dummy
            OutlinedTextField(
                value = "",
                onValueChange = {},
                placeholder = { Text("Cari circle...", color = TextSecondary) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(8.dp)),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.LightGray,
                    focusedBorderColor = PrimaryColor
                )
            )
            Spacer(modifier = Modifier.height(12.dp))

            // List Item Circle
            circles.forEach { circle ->
                CircleItem(circle) { viewModel.toggleCircleSelection(circle.id) }
            }

            // Spacer bawah supaya tidak ketutup tombol
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

// --- Komponen Pendukung Kecil ---

@Composable
fun InputLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Medium,
        color = TextPrimary,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isSingleLine: Boolean = true,
    minLines: Int = 1,
    trailingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = TextSecondary) },
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = Color.LightGray,
            focusedBorderColor = PrimaryColor,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        ),
        singleLine = isSingleLine,
        minLines = minLines,
        trailingIcon = if (trailingIcon != null) {
            { Icon(trailingIcon, contentDescription = null, tint = TextSecondary) }
        } else null
    )
}

@Composable
fun CircleItem(circle: CircleGroup, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = circle.isSelected,
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(checkedColor = PrimaryColor)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Image(
            painter = painterResource(
                id = if (circle.id % 2 == 0) R.drawable.ic_profile1 else R.drawable.ic_profile2
            ),
            contentDescription = "Avatar",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(circle.name, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Text("${circle.memberCount} anggota", fontSize = 12.sp, color = TextSecondary)
        }
    }
}

// Category Selector Component - Dropdown Style
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelector(
    selectedCategory: TitipanCategory,
    onCategorySelected: (TitipanCategory) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedCategory.categoryName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Kategori") },
            leadingIcon = {
                Image(
                    painter = painterResource(id = selectedCategory.iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryColor,
                unfocusedBorderColor = Color(0xFFE5E7EB),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            TitipanCategory.values().forEach { category ->
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Image(
                                painter = painterResource(id = category.iconRes),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = category.categoryName,
                                fontSize = 15.sp
                            )
                        }
                    },
                    onClick = {
                        onCategorySelected(category)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

// Location Picker Card Component
@Composable
fun LocationPickerCard(
    selectedLocation: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Dummy Google Maps Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.alfamart),
                    contentDescription = "Map Preview",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            
            // Selected Location Info
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = PrimaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = if (selectedLocation.isEmpty()) "Pilih Lokasi" else selectedLocation,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (selectedLocation.isEmpty()) TextSecondary else TextPrimary
                    )
                }
                
                if (selectedLocation.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Klik untuk mengubah lokasi",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

// Location Selection Dialog
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationSelectionDialog(
    onLocationSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val dummyLocations = listOf(
        "Alfamart Jakal",
        "Indomaret Pogung",
        "Kantin Fasilkom",
        "Pizza Hut Hartono Mall",
        "Apotek K24 Demangan",
        "KFC Colombo",
        "Kantin Teknik",
        "Starbucks UGM"
    )
    
    AlertDialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    "Pilih Lokasi",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Location List
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    dummyLocations.forEach { location ->
                        Card(
                            onClick = { onLocationSelected(location) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = PrimaryColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = location,
                                    fontSize = 15.sp,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
