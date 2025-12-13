package com.afsar.titipin.ui.circle.add

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.afsar.titipin.data.model.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCircleScreen(
    onBackClick: () -> Unit,
    viewModel: AddCircleViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel.errorMessage) {
        viewModel.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.errorMessage = null
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Buat Circle Baru", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (viewModel.newCircleName.isNotEmpty()) {
                        TextButton(
                            onClick = { viewModel.createCircle(onSuccess = onBackClick) },
                            enabled = !viewModel.isCreating // Disable kalau lagi loading
                        ) {
                            Text(
                                if (viewModel.isCreating) "Loading..." else "BUAT",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                // Muncul hanya jika nama diisi DAN minimal 1 member dipilih
                visible = viewModel.newCircleName.isNotEmpty() &&
                        viewModel.selectedMembers.isNotEmpty() &&
                        !viewModel.isCreating,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.createCircle(onSuccess = onBackClick) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.Add, null) },
                    text = { Text("Buat Circle") }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {

                // Input Nama Circle
                OutlinedTextField(
                    value = viewModel.newCircleName,
                    onValueChange = { viewModel.newCircleName = it },
                    label = { Text("Nama Circle / Grup") },
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    singleLine = true
                )

                HorizontalDivider(thickness = 8.dp, color = Color(0xFFF5F5F5))

                // List Member yang Dipilih (Horizontal)
                if (viewModel.selectedMembers.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(viewModel.selectedMembers) { user ->
                            SelectedMemberChip(
                                user = user,
                                onRemove = { viewModel.removeMemberFromSelection(user) }
                            )
                        }
                    }
                }

                // Search Bar
                OutlinedTextField(
                    value = viewModel.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Cari username teman...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    shape = RoundedCornerShape(24.dp),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedContainerColor = Color(0xFFF0F2F5),
                        unfocusedContainerColor = Color(0xFFF0F2F5)
                    )
                )

                // PERBAIKAN: Indikator Loading saat mencari user
                if (viewModel.isSearching) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

                // Hasil Pencarian
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(viewModel.searchResults) { user ->
                        // Cek apakah user ini sudah dipilih
                        val isSelected = viewModel.selectedMembers.any { it.uid == user.uid }

                        UserSelectionItem(
                            user = user,
                            isSelected = isSelected,
                            onClick = {
                                if (isSelected) viewModel.removeMemberFromSelection(user)
                                else viewModel.addMemberToSelection(user)
                            }
                        )
                    }
                }
            }

            if (viewModel.isCreating) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                        .clickable(enabled = false) {},
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
fun UserSelectionItem(user: User, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
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
            Text(text = user.name.ifEmpty { "Tanpa Nama" }, fontWeight = FontWeight.SemiBold)
            Text(text = "@${user.username}", fontSize = 14.sp, color = Color.Gray)
        }

        // Checkbox Visual
        if (isSelected) {
            Icon(Icons.Default.Check, contentDescription = "Selected", tint = Color(0xFF008069))
        }
    }
}

@Composable
fun SelectedMemberChip(user: User, onRemove: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(60.dp)) {
        Box {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(user.photoUrl.ifEmpty { null }).crossfade(true).build(),
                contentDescription = null,
                placeholder = rememberVectorPainter(Icons.Default.Person),
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(50.dp).clip(CircleShape).background(Color.LightGray)
            )

            // Tombol X Merah Kecil
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-4).dp) // Sedikit keluar agar tidak menutupi muka
                    .size(20.dp)
                    .background(Color.Red, CircleShape)
                    .clickable { onRemove() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove",
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = user.name.split(" ").firstOrNull() ?: "",
            fontSize = 12.sp,
            maxLines = 1,
            fontWeight = FontWeight.Medium
        )
    }
}
//package com.afsar.titipin.ui.circle.add
//
//import androidx.compose.animation.AnimatedVisibility
//import androidx.compose.animation.fadeIn
//import androidx.compose.animation.fadeOut
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.LazyRow
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Add
//import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.material.icons.filled.Check
//import androidx.compose.material.icons.filled.Close
//import androidx.compose.material.icons.filled.Person
//import androidx.compose.material.icons.filled.Search
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.vector.rememberVectorPainter
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.hilt.navigation.compose.hiltViewModel
//import coil.compose.AsyncImage
//import coil.request.ImageRequest
//import com.afsar.titipin.data.model.User
//import com.afsar.titipin.ui.home.viewmodel.CircleViewModel
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun AddCircleScreen(
//    onBackClick: () -> Unit,
//    viewModel: AddCircleViewModel = hiltViewModel()
//) {
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = {
//                    Column (
//                        horizontalAlignment = Alignment.CenterHorizontally
//                    ){
//                        Text("Buat Circle Baru", fontSize = 18.sp, fontWeight = FontWeight.Bold)
//                    }
//                },
//                navigationIcon = {
//                    IconButton(onClick = onBackClick) {
//                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
//                    }
//                },
//                actions = {
//                    if (viewModel.newCircleName.isNotEmpty()) {
//                        TextButton(onClick = {
//                            viewModel.createCircle(onSuccess = onBackClick)
//                        }) {
//                            Text("BUAT", color = Color.White, fontWeight = FontWeight.Bold)
//                        }
//                    }
//                },
//            )
//        },
//
//        floatingActionButton = {
//            AnimatedVisibility(
//                visible = (viewModel.newCircleName.isNotEmpty() && viewModel.selectedMembers.isNotEmpty())&&!viewModel.isCreating,
//                enter = fadeIn(),
//                exit = fadeOut()
//            ) { FloatingActionButton(
//                onClick = {
//                    viewModel.createCircle(onSuccess = onBackClick)
//                }
//            ) {
//                Icon(
//                    Icons.Default.Add,
//                    contentDescription = "Buat Circle Baru",
//                    modifier = Modifier.size(24.dp),
//                    tint = Color.White
//                )
//            }
//        }
//
//        }
//    ) { padding ->
//        Column(
//            modifier = Modifier
//                .padding(padding)
//                .fillMaxSize()
//        ) {
//            OutlinedTextField(
//                value = viewModel.newCircleName,
//                onValueChange = { viewModel.newCircleName = it },
//                label = { Text("Nama Circle / Grup") },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(16.dp),
//                singleLine = true
//            )
//
//            Divider(thickness = 8.dp, color = Color(0xFFF5F5F5))
//
//            if (viewModel.selectedMembers.isNotEmpty()) {
//                LazyRow(
//                    modifier = Modifier.fillMaxWidth().padding(16.dp),
//                    horizontalArrangement = Arrangement.spacedBy(8.dp)
//                ) {
//                    items(viewModel.selectedMembers) { user ->
//                        SelectedMemberChip(
//                            user = user,
//                            onRemove = { viewModel.removeMemberFromSelection(user) }
//                        )
//                    }
//                }
//            }
//
//            OutlinedTextField(
//                value = viewModel.searchQuery,
//                onValueChange = { viewModel.onSearchQueryChange(it) },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = 16.dp, vertical = 8.dp),
//                placeholder = { Text("Cari username teman...") },
//                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
//                shape = RoundedCornerShape(24.dp),
//                colors = TextFieldDefaults.colors(
//                    focusedIndicatorColor = Color.Transparent,
//                    unfocusedIndicatorColor = Color.Transparent,
//                    focusedContainerColor = Color(0xFFF0F2F5),
//                    unfocusedContainerColor = Color(0xFFF0F2F5)
//                )
//            )
//
//            LazyColumn(modifier = Modifier.weight(1f)) {
//                items(viewModel.searchResults) { user ->
//                    val isSelected = viewModel.selectedMembers.contains(user)
//
//                    UserSelectionItem(
//                        user = user,
//                        isSelected = isSelected,
//                        onClick = {
//                            if (isSelected) viewModel.removeMemberFromSelection(user)
//                            else viewModel.addMemberToSelection(user)
//                        }
//                    )
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun UserSelectionItem(user: User, isSelected: Boolean, onClick: () -> Unit) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .clickable { onClick() }
//            .padding(horizontal = 16.dp, vertical = 12.dp),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        // Avatar
//        AsyncImage(
//            model = ImageRequest.Builder(LocalContext.current)
//                .data(user.photoUrl.ifEmpty { null })
//                .crossfade(true).build(),
//            contentDescription = null,
//            placeholder = rememberVectorPainter(Icons.Default.Person),
//            error = rememberVectorPainter(Icons.Default.Person),
//            contentScale = ContentScale.Crop,
//            modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.LightGray)
//        )
//
//        Spacer(modifier = Modifier.width(16.dp))
//
//        Column(modifier = Modifier.weight(1f)) {
//            Text(text = user.name.ifEmpty { "Tanpa Nama" }, fontWeight = FontWeight.SemiBold)
//            Text(text = "@${user.username}", fontSize = 14.sp, color = Color.Gray)
//        }
//
//        if (isSelected) {
//            Icon(Icons.Default.Check, contentDescription = "Selected", tint = Color(0xFF008069))
//        }
//    }
//}
//
//@Composable
//fun SelectedMemberChip(user: User, onRemove: () -> Unit) {
//    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(60.dp)) {
//        Box {
//            AsyncImage(
//                model = ImageRequest.Builder(LocalContext.current)
//                    .data(user.photoUrl.ifEmpty { null }).crossfade(true).build(),
//                contentDescription = null,
//                placeholder = rememberVectorPainter(Icons.Default.Person),
//                contentScale = ContentScale.Crop,
//                modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.LightGray)
//            )
//            Icon(
//                imageVector = Icons.Default.Close,
//                contentDescription = "Remove",
//                tint = Color.White,
//                modifier = Modifier
//                    .size(16.dp)
//                    .align(Alignment.TopEnd)
//                    .background(Color.Gray, CircleShape)
//                    .clickable { onRemove() }
//            )
//        }
//        Text(
//            text = user.name.split(" ").firstOrNull() ?: "",
//            fontSize = 12.sp,
//            maxLines = 1,
//            fontWeight = FontWeight.Medium
//        )
//    }
//}