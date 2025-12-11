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
import kotlinx.coroutines.delay

@Composable
fun ItemAssignmentScreen(
    onBackClick: () -> Unit = {},
    onNavigateToPayment: () -> Unit = {}
) {
    var assignmentItems by remember {
        mutableStateOf(
            listOf(
                AssignmentItem("Indomie Goreng", 2, "Budi Santoso", "Rasa rendang", true, R.drawable.ic_profile1),
                AssignmentItem("Susu Ultra Milk Cokelat 1L", 1, null, "", false, null),
                AssignmentItem("Teh Botol Kotak", 3, "Citra Lestari", "", true, R.drawable.ic_profile2),
                AssignmentItem("Air Mineral", 2, null, "", false, null)
            )
        )
    }
    
    val assignedCount = assignmentItems.count { it.isAssigned }
    val totalCount = assignmentItems.size
    val progress = assignedCount.toFloat() / totalCount.toFloat()
    
    // Auto-navigate when all assigned
    LaunchedEffect(assignedCount) {
        if (assignedCount == totalCount) {
            delay(800)
            onNavigateToPayment()
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
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
                        text = "Assign Barang Belanjaan",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                }
                
                // Progress Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Progress Assign",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "$assignedCount/$totalCount",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (assignedCount == totalCount) Color(0xFF4CAF50) else PrimaryColor
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            LinearProgressIndicator(
                                progress = progress,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp)),
                                color = if (assignedCount == totalCount) Color(0xFF4CAF50) else PrimaryColor,
                                trackColor = Color(0xFFE0E0E0)
                            )
                        }
                    }
                }
                
                // Assignment Items
                items(assignmentItems) { item ->
                    AssignmentItemCard(
                        item = item,
                        onAssignClick = {
                            // Toggle assignment for demo
                            assignmentItems = assignmentItems.map {
                                if (it.itemName == item.itemName && !it.isAssigned) {
                                    it.copy(
                                        isAssigned = true,
                                        assignedTo = "Siti",
                                        avatarRes = R.drawable.ic_profile2
                                    )
                                } else it
                            }
                        }
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun AssignmentItemCard(
    item: AssignmentItem,
    onAssignClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isAssigned) Color(0xFFE8F5E9) else Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.itemName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${item.quantity}x",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (item.isAssigned && item.assignedTo != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item.avatarRes?.let {
                        Image(
                            painter = painterResource(id = it),
                            contentDescription = null,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                    
                    Column {
                        Text(
                            text = item.assignedTo,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                        if (item.notes.isNotEmpty()) {
                            Text(
                                text = "Catatan: ${item.notes}",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Button(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    enabled = false
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(  18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Assigned", fontSize = 14.sp)
                }
            } else {
                Text(
                    text = "Belum di-assign",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedButton(
                    onClick = onAssignClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = PrimaryColor
                    ),
                    border = BorderStroke(1.dp, PrimaryColor),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Assign Penitip", fontSize = 14.sp)
                }
            }
        }
    }
}

// Data class
data class AssignmentItem(
    val itemName: String,
    val quantity: Int,
    val assignedTo: String?,
    val notes: String,
    val isAssigned: Boolean,
    val avatarRes: Int?
)
