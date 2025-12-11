package com.example.titipin.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.titipin.model.Transaction
import com.example.titipin.model.WeeklyStat
import com.example.titipin.ui.theme.*

@Composable
fun WeeklyChartItem(stat: WeeklyStat, isActive: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
        modifier = Modifier.height(60.dp)
    ) {
        Box(
            contentAlignment = Alignment.BottomCenter,
            modifier = Modifier
                .width(10.dp)
                .height(40.dp)
                .clip(RoundedCornerShape(50))
                .background(if (isActive) AccentColor.copy(alpha = 0.2f) else PrimaryColor.copy(alpha = 0.2f))
        ) {
            if (isActive) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(stat.percentage)
                        .clip(RoundedCornerShape(50))
                        .background(AccentColor)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = stat.day, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
    }
}

@Composable
fun TransactionItemCard(transaction: Transaction) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .background(CardLight, RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFFE1DAE7), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(BgLight, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                val icon = when(transaction.iconType) {
                    "food" -> Icons.Default.Fastfood
                    "coffee" -> Icons.Default.LocalCafe
                    "box" -> Icons.Default.Inventory2
                    else -> Icons.Default.LocalPizza
                }
                Icon(icon, contentDescription = null, tint = TextPrimary)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = transaction.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Text(
                    text = transaction.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
        Text(
            text = transaction.amount,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            textDecoration = if (transaction.isCancelled) TextDecoration.LineThrough else TextDecoration.None,
            color = if (transaction.isCancelled) TextSecondary else TextPrimary
        )
    }
}