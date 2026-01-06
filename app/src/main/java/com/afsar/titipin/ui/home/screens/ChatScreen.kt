package com.afsar.titipin.ui.home.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.afsar.titipin.ui.components.molecules.ChatBubbleGroup
import com.afsar.titipin.ui.home.viewmodel.ChatCircleViewModel
import com.afsar.titipin.ui.theme.OrangePrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatCircleScreen(
    onBackClick: () -> Unit,
    viewModel: ChatCircleViewModel = hiltViewModel()
) {
    val messages = viewModel.messages
    val circleName = viewModel.circleName
    val circleAvatar = viewModel.circleAvatar
    val currentUser = viewModel.currentUserId

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto scroll ke bawah saat pesan baru masuk
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        // --- TOP BAR ---
        topBar = {
            Surface(
                shadowElevation = 2.dp,
                color = Color.White
            ) {
                Row(
                    modifier = Modifier
                        .statusBarsPadding()
                        .height(64.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = OrangePrimary)
                    }

                    // Avatar Circle
                    AsyncImage(
                        model = circleAvatar.ifEmpty { "https://ui-avatars.com/api/?name=$circleName" },
                        contentDescription = null,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(10.dp))

                    // Info Circle
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = circleName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${viewModel.memberCount} anggota",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        },

        // --- INPUT BAR ---
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                color = Color.White,
                modifier = Modifier.imePadding() // Agar naik saat keyboard muncul
            ) {
                Row(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Kolom Input Bulat
                    TextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Ketik pesan...", fontSize = 14.sp, color = Color.Gray) },
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(24.dp))
                            .heightIn(min = 48.dp, max = 100.dp), // Bisa multiline
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF5F5F5),
                            unfocusedContainerColor = Color(0xFFF5F5F5),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = OrangePrimary
                        ),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Tombol Kirim Bulat
                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                viewModel.sendMessage(inputText)
                                inputText = ""
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(OrangePrimary, CircleShape),
                        enabled = inputText.isNotBlank()
                    ) {
                        Icon(Icons.Default.Send, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }
        },
        containerColor = Color(0xFFEFEFEF) // Warna background chat (abu muda)
    ) { padding ->

        // --- DAFTAR PESAN ---
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(16.dp)) }

            items(messages) { msg ->
                ChatBubbleGroup(
                    message = msg,
                    isMe = msg.senderId == currentUser
                )
            }
        }
    }
}