package com.example.titipin.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.titipin.ui.theme.*
import com.example.titipin.data.model.AssignableItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemAssignmentScreen(
    onBackClick: () -> Unit = {},
    onNavigateToPayment: () -> Unit = {}
) {
    // Dummy Data
    val users = listOf("Azhartama", "Uqi", "Marsha", "Yuman")
    
    // Items to assign
    val items = remember { mutableStateListOf(
        AssignableItem("Cheezy Freezy M", "25000", 1, 0, false),
        AssignableItem("Red Bull M", "25000", 1, 0, false),
        AssignableItem("Lemon Tea", "11000", 1, 0, false)
    ) }

    var showAssignDialog by remember { mutableStateOf<AssignableItem?>(null) }

    Scaffold(
        containerColor = BgLight,
        topBar = {
            TopAppBar(
                title = { Text("Item Tagihan & Alokasi", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Button(
                onClick = onNavigateToPayment,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
            ) {
                Text(
                    text = "Lanjut ke Pembayaran", 
                    fontSize = 16.sp, 
                    fontWeight = FontWeight.Bold
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items.forEach { item ->
                AssignmentCard(item = item, onAssignClick = { showAssignDialog = item })
            }
        }

        // Dialog for Assignment
        showAssignDialog?.let { item ->
            AssignmentDialog(
                item = item,
                users = users,
                onDismiss = { showAssignDialog = null },
                onDone = { assignments ->
                    // Update item assignments (Dummy logic: just update assigned count)
                    item.assignedCount = assignments.values.sum()
                    item.isAssigned = item.assignedCount == item.totalUnits
                    showAssignDialog = null
                }
            )
        }
    }
}

@Composable
fun AssignmentCard(item: com.example.titipin.data.model.AssignableItem, onAssignClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Edit, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(item.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Flex Row for inputs
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Price input
                OutlinedTextField(
                    value = item.price,
                    onValueChange = {},
                    label = { Text("per unit") },
                    prefix = { Text("Rp ") },
                    modifier = Modifier.weight(1f),
                    readOnly = true, // Dummy
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFFF9F9F9),
                        focusedContainerColor = Color(0xFFF9F9F9)
                    )
                )
                
                // Qty input
                OutlinedTextField(
                    value = item.totalUnits.toString(),
                    onValueChange = {},
                    label = { Text("total units") },
                    prefix = { Text("# ") },
                    modifier = Modifier.weight(0.7f),
                    readOnly = true, // Dummy
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFFF9F9F9),
                        focusedContainerColor = Color(0xFFF9F9F9)
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Assign Button / Status
            Button(
                onClick = onAssignClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (item.isAssigned) Color(0xFFE8F5E9) else Color(0xFFEEEEEE),
                    contentColor = if (item.isAssigned) Color(0xFF2E7D32) else TextSecondary
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth(),
                elevation = null
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                         if (item.isAssigned) "Assigned: ${item.assignedCount}/${item.totalUnits} units"
                         else "Assign items"
                    )
                }
            }
        }
    }
}

@Composable
fun AssignmentDialog(
    item: com.example.titipin.data.model.AssignableItem,
    users: List<String>,
    onDismiss: () -> Unit,
    onDone: (Map<String, Int>) -> Unit
) {
    val assignments = remember { mutableStateMapOf<String, Int>().apply { users.forEach { put(it, 0) } } }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Assign \"${item.name}\"",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Total units: ${item.totalUnits}",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // User List
                users.forEach { user ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(user, fontWeight = FontWeight.Medium)
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { 
                                val current = assignments[user] ?: 0
                                if (current > 0) assignments[user] = current - 1 
                            }) {
                                Icon(Icons.Default.Remove, null, tint = PrimaryColor)
                            }
                            
                            Text(
                                text = (assignments[user] ?: 0).toString(),
                                modifier = Modifier.width(24.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            
                            IconButton(onClick = { 
                                val current = assignments[user] ?: 0
                                assignments[user] = current + 1 
                            }) {
                                Icon(Icons.Default.Add, null, tint = PrimaryColor)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = { onDone(assignments) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                ) {
                    Text("Done")
                }
            }
        }
    }
}

