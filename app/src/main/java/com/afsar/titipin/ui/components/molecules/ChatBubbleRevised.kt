package com.afsar.titipin.ui.components.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.afsar.titipin.data.model.ChatMessage
import com.afsar.titipin.data.model.ChatMessages
import com.afsar.titipin.ui.theme.OrangePrimary

@Composable
fun ChatBubbleGroup(message: ChatMessages, isMe: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        // --- AVATAR LAWAN (KIRI) ---
        if (!isMe) {
            AsyncImage(
                model = message.avatarUrl.ifEmpty { "https://ui-avatars.com/api/?name=${message.senderName}" },
                contentDescription = null,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        // --- BUBBLE PESAN ---
        Column(
            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start,
            modifier = Modifier.weight(1f, fill = false)
        ) {
            if (!isMe) {
                Text(
                    text = message.senderName,
                    fontSize = 11.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 2.dp, start = 4.dp)
                )
            }

            Surface(
                color = if (isMe) OrangePrimary else Color.White,
                shadowElevation = 1.dp,
                shape = if (isMe) RoundedCornerShape(16.dp, 16.dp, 2.dp, 16.dp)
                else RoundedCornerShape(16.dp, 16.dp, 16.dp, 2.dp),
                modifier = Modifier.widthIn(max = 280.dp)
            ) {
                // BOX ADALAH KUNCI AGAR ALIGNMENT BOTTOM END BEKERJA
                Box(modifier = Modifier.padding(start = 12.dp, top = 8.dp, end = 12.dp, bottom = 8.dp)) {

                    // 1. Teks Pesan
                    Text(
                        text = message.message,
                        color = if (isMe) Color.White else Color.Black,
                        fontSize = 14.sp,
                        // Beri padding bawah/kanan agar tidak menabrak jam
                        modifier = Modifier.padding(bottom = 10.dp, end = 24.dp)
                    )

                    // 2. Teks Jam (Overlay di pojok kanan bawah Box)
                    Text(
                        text = message.timestamp ?: "",
                        fontSize = 10.sp,
                        color = if (isMe) Color.White.copy(alpha = 0.7f) else Color.Gray,
                        modifier = Modifier.align(Alignment.BottomEnd) // ✅ Sekarang Valid karena di dalam Box
                    )
                }
            }
        }
    }
}