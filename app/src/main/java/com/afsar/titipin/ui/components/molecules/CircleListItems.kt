package com.afsar.titipin.ui.components.molecules

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.afsar.titipin.data.model.ChatMessages
import com.afsar.titipin.data.model.CircleGroup
import com.afsar.titipin.data.model.ContactUser
import com.afsar.titipin.ui.theme.*

@Composable
fun CircleItem(group: CircleGroup, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = group.avatarRes),
                contentDescription = null,
                modifier = Modifier.size(50.dp).clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(group.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)

                Text(
                    text = "${group.lastSender}: ${group.lastMessage}",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (group.unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .background(OrangePrimary, CircleShape)

                        .defaultMinSize(minWidth = 30.dp, minHeight = 30.dp)
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = group.unreadCount.toString(),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
//
//// 2. CHAT BUBBLE (Untuk Halaman Chat)
//@Composable
//fun ChatBubble(message: ChatMessages) {
//    val alignment = if (message.isMe) Alignment.End else Alignment.Start
//    val bubbleColor = Color.White
//    val textColor = TextPrimary
//
//    Column(
//        modifier = Modifier.fillMaxWidth(),
//        horizontalAlignment = alignment
//    ) {
//
//        if (!message.isMe) {
//            Text(message.senderName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.padding(start = 52.dp, bottom = 4.dp))
//        }
//
//        Row(verticalAlignment = Alignment.Top) {
//
//            if (!message.isMe) {
//                Image(
//                    painter = painterResource(id = message.avatarRes),
//                    contentDescription = null,
//                    modifier = Modifier.size(40.dp).clip(CircleShape),
//                    contentScale = ContentScale.Crop
//                )
//                Spacer(modifier = Modifier.width(8.dp))
//            }
//
//            // Bubble
//            Surface(
//                color = bubbleColor,
//                shape = if (message.isMe) RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
//                else RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp),
//                shadowElevation = 1.dp,
//                modifier = Modifier.widthIn(max = 280.dp)
//            ) {
//                Text(
//                    text = message.text,
//                    modifier = Modifier.padding(12.dp),
//                    color = textColor,
//                    fontSize = 14.sp
//                )
//            }
//        }
//        Spacer(modifier = Modifier.height(12.dp))
//    }
//}

// 3. CONTACT ITEM (Untuk Halaman Create Circle)
@Composable
fun ContactItem(contact: ContactUser, onSelect: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = contact.avatarRes),
            contentDescription = null,
            modifier = Modifier.size(48.dp).clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(contact.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.weight(1f))

        // Checkbox Custom (Lingkaran Orange)
        if (contact.isSelected) {
            Box(
                modifier = Modifier.size(24.dp).background(OrangePrimary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
        } else {
            Box(
                modifier = Modifier.size(24.dp).background(Color(0xFFFFF4E3), CircleShape)
            )
        }
    }
}