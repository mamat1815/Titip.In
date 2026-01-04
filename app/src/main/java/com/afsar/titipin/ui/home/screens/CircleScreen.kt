package com.afsar.titipin.ui.home.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.afsar.titipin.data.model.Circle
import com.afsar.titipin.data.model.User
// import com.afsar.titipin.ui.components.molecules.LoadingOverlay // Hapus/Comment jika tidak dipakai di root
import com.afsar.titipin.ui.home.viewmodel.CircleListViewModel
import com.afsar.titipin.ui.theme.OrangePrimary
import com.afsar.titipin.ui.theme.TextPrimary
import com.afsar.titipin.ui.theme.TextSecondary
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CircleScreen(
    // navController: NavController, // Aktifkan jika sudah siap navigasi
    viewModel: CircleListViewModel = hiltViewModel()
) {
    val circles = viewModel.circles
    var searchQuery by remember { mutableStateOf("") }

    // Bottom Sheet State
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // FIX 1: Box HARUS ADA sebagai container utama dan background
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF9FAFB))) {

        Scaffold(
            containerColor = Color.Transparent,
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        viewModel.resetSelection()
                        showBottomSheet = true
                    },
                    containerColor = OrangePrimary,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    // Padding agar tidak ketutup BottomNav (sesuaikan dengan tinggi nav bar kamu)
                    modifier = Modifier.padding(bottom = 90.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Circle")
                }
            },
            modifier = Modifier.fillMaxSize()
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                Text("Circle Saya", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(16.dp))

                // Search Bar
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari Circle...", color = TextSecondary) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = OrangePrimary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = OrangePrimary
                    ),
                    shape = RoundedCornerShape(100)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // List Circle
                if (circles.isEmpty() && !viewModel.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Belum ada circle.", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        // Padding bottom extra biar list paling bawah tidak ketutup FAB/Nav
                        contentPadding = PaddingValues(bottom = 120.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val filteredCircles = if (searchQuery.isBlank()) circles else circles.filter { it.name.contains(searchQuery, true) }

                        items(filteredCircles) { group ->
                            CircleItem(group = group) {
                                // TODO: Navigasi ke Detail
                                // navController.navigate(Screen.ChatDetail.createRoute(group.id))
                            }
                        }
                    }
                }
            }
        }

        // --- BOTTOM SHEET UNTUK BUAT CIRCLE ---
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState,
                containerColor = Color(0xFFF7F7F7),
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                CreateCircleSheetContent(
                    viewModel = viewModel,
                    onDismiss = { showBottomSheet = false },
                    onCreateSuccess = { showBottomSheet = false }
                )
            }
        }
    } // End of Box
}

// --- ITEM LIST CIRCLE ---
@Composable
fun CircleItem(group: Circle, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Surface(
                modifier = Modifier.size(50.dp),
                shape = CircleShape,
                color = if (group.isActiveSession) Color(0xFFE8F5E9) else Color(0xFFECEFF1)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (group.isActiveSession) Icons.Default.ShoppingBag else Icons.Default.Group,
                        contentDescription = null,
                        tint = if (group.isActiveSession) Color(0xFF008069) else Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = group.name,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    // Timestamp
                    group.lastMessageTime?.let {
                        Text(
                            text = formatTime(it),
                            fontSize = 11.sp,
                            color = if (group.isActiveSession) Color(0xFF008069) else Color.Gray
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = if(group.isActiveSession) "● Sesi Belanja Aktif!" else group.lastMessage.ifEmpty { "Grup dibuat" },
                    fontSize = 14.sp,
                    color = if(group.isActiveSession) Color(0xFF008069) else TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// --- KONTEN BOTTOM SHEET ---
@Composable
fun CreateCircleSheetContent(
    viewModel: CircleListViewModel,
    onDismiss: () -> Unit,
    onCreateSuccess: () -> Unit
) {
    var currentStep by remember { mutableIntStateOf(1) }
    var searchContactQuery by remember { mutableStateOf("") }
    var groupNameInput by remember { mutableStateOf("") }

    val selectedContacts = viewModel.selectedContacts
    val searchResults = viewModel.searchContactResults

    // Logic Image Picker
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> selectedImageUri = uri }

    Column(
        modifier = Modifier
            .fillMaxHeight(0.9f)
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        // HEADER NAVIGASI SHEET
        Box(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp).height(48.dp)
        ) {
            TextButton(
                onClick = { if (currentStep == 1) onDismiss() else currentStep = 1 },
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Text(if (currentStep == 1) "Batal" else "Kembali", color = Color.Gray)
            }

            Text(
                text = if(currentStep == 1) "Pilih Anggota" else "Info Circle",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )

            TextButton(
                onClick = {
                    if (currentStep == 1) {
                        currentStep = 2
                    } else {
                        // Logic Submit
                        var finalName = groupNameInput.trim()
                        if (finalName.isBlank()) {
                            finalName = selectedContacts.joinToString(", ") { it.name }.take(20)
                        }
                        if (finalName.isBlank()) finalName = "New Circle"

                        viewModel.createCircle(finalName, selectedImageUri) {
                            onCreateSuccess()
                        }
                    }
                },
                modifier = Modifier.align(Alignment.CenterEnd),
                enabled = if(currentStep == 1) selectedContacts.isNotEmpty() else !viewModel.isCreating
            ) {

                if (viewModel.isCreating) {
                    // FIX 2: Jangan pakai LoadingOverlay di sini karena ini dalam Button
                    // Pakai CircularProgressIndicator kecil saja
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = OrangePrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(if (currentStep == 1) "Lanjut" else "Buat", fontWeight = FontWeight.Bold, color = OrangePrimary)
                }
            }
        }

        // KONTEN
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            if (currentStep == 1) {
                // ... (KODE STEP 1 SAMA SEPERTI YANG SAYA BERIKAN SEBELUMNYA) ...
                // STEP 1: CARI & PILIH KONTAK

                // Chips Kontak Terpilih
                if (selectedContacts.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        items(selectedContacts) { user ->
                            SelectedContactChip(user = user) {
                                viewModel.removeContact(user)
                            }
                        }
                    }
                }

                // Search Bar
                TextField(
                    value = searchContactQuery,
                    onValueChange = {
                        searchContactQuery = it
                        viewModel.searchContacts(it)
                    },
                    placeholder = { Text("Cari username...", fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // List Hasil Search
                LazyColumn {
                    items(searchResults) { user ->
                        ContactItem(user = user, isSelected = false) {
                            viewModel.toggleContactSelection(user)
                            searchContactQuery = "" // Reset search setelah pilih
                        }
                    }
                }
            } else {
                // ... (KODE STEP 2 SAMA SEPERTI YANG SAYA BERIKAN SEBELUMNYA) ...
                // STEP 2: NAMA & FOTO
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(top = 24.dp)) {

                    // Image Picker
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEEEEEE))
                            .clickable { launcher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedImageUri != null) {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.Gray)
                        }
                    }

                    Text("Upload Foto", fontSize = 12.sp, color = OrangePrimary, modifier = Modifier.padding(top = 8.dp))

                    Spacer(modifier = Modifier.height(24.dp))

                    // Input Nama
                    OutlinedTextField(
                        value = groupNameInput,
                        onValueChange = { groupNameInput = it },
                        label = { Text("Nama Circle") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text("${selectedContacts.size} Anggota dipilih", color = Color.Gray, fontSize = 12.sp)
                }
            }
        }
    }
}

// ... (Komponen ContactItem, SelectedContactChip, formatTime sama seperti sebelumnya) ...
@Composable
fun ContactItem(user: User, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(user.photoUrl.ifEmpty { null })
                .crossfade(true).build(),
            contentDescription = null,
            placeholder = rememberVectorPainter(Icons.Default.Person),
            error = rememberVectorPainter(Icons.Default.Person),
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.LightGray)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(user.name.ifEmpty { "Tanpa Nama" }, fontWeight = FontWeight.SemiBold)
            Text("@${user.username}", fontSize = 12.sp, color = Color.Gray)
        }
        if (isSelected) {
            Icon(Icons.Default.Check, contentDescription = null, tint = OrangePrimary)
        }
    }
}

@Composable
fun SelectedContactChip(user: User, onRemove: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFE0E0E0),
        onClick = onRemove
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 4.dp, end = 8.dp, top = 4.dp, bottom = 4.dp)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(user.photoUrl.ifEmpty { null }).build(),
                contentDescription = null,
                placeholder = rememberVectorPainter(Icons.Default.Person),
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(24.dp).clip(CircleShape).background(Color.Gray)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(user.name.split(" ").first(), fontSize = 12.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
        }
    }
}

fun formatTime(timestamp: Timestamp): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(timestamp.toDate())
}