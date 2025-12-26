package com.afsar.titipin.ui.components.molecules

import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.afsar.titipin.data.model.ChatMessages

import com.afsar.titipin.ui.theme.*
@Composable
fun ChatBubbleRevised(message: ChatMessages) {
    val isMe = message.isMe

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        // --- AVATAR LAWAN (KIRI) ---
        if (!isMe) {
            Image(
                painter = painterResource(id = message.avatarRes),
                contentDescription = null,
                modifier = Modifier.size(36.dp).clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        // --- BUBBLE ---
        Column(
            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start,
            modifier = Modifier.weight(1f, fill = false)
        ) {
            // Nama Pengirim (Hanya Grup/Lawan)
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
                shape = if (isMe) {
                    RoundedCornerShape(16.dp, 0.dp, 16.dp, 16.dp)
                } else {
                    RoundedCornerShape(0.dp, 16.dp, 16.dp, 16.dp)
                },
                modifier = Modifier.widthIn(max = 260.dp)
            ) {
                Box(modifier = Modifier.padding(start = 12.dp, top = 12.dp, end = 12.dp, bottom = 8.dp)) {
                    Column {
                        Text(
                            text = message.text,

                            color = if (isMe) Color.White else Color.Black,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        // Timestamp
                        Text(
                            text = message.timestamp,
                            fontSize = 10.sp,

                            color = if (isMe) Color.White.copy(alpha = 0.8f) else Color.Gray,
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }
            }
        }

        // --- AVATAR SAYA (KANAN) ---
        if (isMe) {
            Spacer(modifier = Modifier.width(8.dp))
            Image(
                painter = painterResource(id = message.avatarRes),
                contentDescription = null,
                modifier = Modifier.size(36.dp).clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }
    }
}