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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.titipin.R
import com.example.titipin.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SessionChatScreen(
    onBackClick: () -> Unit = {}
) {
    var messageText by remember { mutableStateOf("") }
    
    val messages = remember {
        listOf(
            ChatMessage(
                id = 1,
                senderName = "Budi Santoso",
                message = "Halo, jangan lupa pesanan saya ya",
                timestamp = "10:30",
                isFromMe = false,
                avatarRes = R.drawable.ic_profile1
            ),
            ChatMessage(
                id = 2,
                senderName = "Anda",
                message = "Siap, pesanan sedang diproses!",
                timestamp = "10:32",
                isFromMe = true,
                avatarRes = null
            ),
            ChatMessage(
                id = 3,
                senderName = "Citra Lestari",
                message = "Kak, seblak cekernya level berapa?",
                timestamp = "10:35",
                isFromMe = false,
                avatarRes = R.drawable.ic_profile2
            ),
            ChatMessage(
                id = 4,
                senderName = "Anda",
                message = "Level 2 ya sesuai catatan",
                timestamp = "10:36",
                isFromMe = true,
                avatarRes = null
            ),
            ChatMessage(
                id = 5,
                senderName = "Budi Santoso",
                message = "Terima kasih!",
                timestamp = "10:38",
                isFromMe = false,
                avatarRes = R.drawable.ic_profile1
            )
        )
    }
    
    Scaffold(
        containerColor = Color(0xFFF5F5F5),
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .statusBarsPadding()
                    .height(56.dp)
                    .padding(horizontal = 16.dp)
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Text(
                    text = "Chat Sesi",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        },
        bottomBar = {
            ChatInputBar(
                message = messageText,
                onMessageChange = { messageText = it },
                onSendClick = {
                    // Handle send message
                    messageText = ""
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5)),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(messages) { message ->
                ChatMessageBubble(message)
            }
        }
    }
}

@Composable
fun ChatMessageBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isFromMe) Arrangement.End else Arrangement.Start
    ) {
        if (!message.isFromMe) {
            // Avatar for other users
            message.avatarRes?.let {
                Image(
                    painter = painterResource(id = it),
                    contentDescription = null,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
        
        Column(
            modifier = Modifier.widthIn(max = 280.dp),
            horizontalAlignment = if (message.isFromMe) Alignment.End else Alignment.Start
        ) {
            if (!message.isFromMe) {
                Text(
                    text = message.senderName,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (message.isFromMe) PrimaryColor else Color.White
                ),
                shape = RoundedCornerShape(
                    topStart = 12.dp,
                    topEnd = 12.dp,
                    bottomStart = if (message.isFromMe) 12.dp else 4.dp,
                    bottomEnd = if (message.isFromMe) 4.dp else 12.dp
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Text(
                    text = message.message,
                    fontSize = 14.sp,
                    color = if (message.isFromMe) Color.White else TextPrimary,
                    modifier = Modifier.padding(12.dp),
                    lineHeight = 20.sp
                )
            }
            
            Text(
                text = message.timestamp,
                fontSize = 11.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun ChatInputBar(
    message: String,
    onMessageChange: (String) -> Unit,
    onSendClick: () -> Unit
) {
    Surface(
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = message,
                onValueChange = onMessageChange,
                placeholder = { Text("Ketik pesan...", fontSize = 14.sp) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryColor,
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color(0xFFF5F5F5)
                ),
                singleLine = true
            )
            
            IconButton(
                onClick = {
                    if (message.isNotBlank()) {
                        onSendClick()
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (message.isNotBlank()) PrimaryColor else Color(0xFFE0E0E0),
                        CircleShape
                    )
            ) {
                Icon(
                    Icons.Default.Send,
                    contentDescription = "Send",
                    tint = if (message.isNotBlank()) Color.White else TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// Data class
data class ChatMessage(
    val id: Int,
    val senderName: String,
    val message: String,
    val timestamp: String,
    val isFromMe: Boolean,
    val avatarRes: Int? = null
)
