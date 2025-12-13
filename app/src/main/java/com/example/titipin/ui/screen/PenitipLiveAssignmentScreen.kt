package com.example.titipin.ui.screen

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.titipin.ui.theme.*
import com.example.titipin.ui.viewmodel.PenitipLiveAssignmentViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import com.example.titipin.data.model.AssignableItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PenitipLiveAssignmentScreen(
    onBackClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
    onAllAssigned: () -> Unit = {},
    viewModel: PenitipLiveAssignmentViewModel = viewModel()
) {
    val users = listOf("Azhartama", "Uqi", "Marsha", "Yuman")
    
    val items by viewModel.items.collectAsState()
    val allAssigned by viewModel.allAssigned.collectAsState()

    var showAssignDialog by remember { mutableStateOf<AssignableItem?>(null) }

    Scaffold(
        containerColor = BgLight,
        topBar = {
            TopAppBar(
                title = { Text("Pembagian Item", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onChatClick) {
                        Icon(Icons.Default.Chat, "Chat", tint = PrimaryColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            if (allAssigned) {
                Button(
                    onClick = onAllAssigned,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                ) {
                    Text("Semua Item Terbagi - Lanjut", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
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
            // Receipt Photo Card (Read-only)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Foto Struk Belanja", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Image,
                            null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray
                        )
                    }
                    Text(
                        "(Foto diunggah oleh pembeli)",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Text("Daftar Belanja Hasil Scan", fontSize = 18.sp, fontWeight = FontWeight.Bold)

            // Items List
            items.forEach { item ->
                AssignmentCardPenitip(item = item, onAssignClick = { showAssignDialog = item })
            }
        }

        // Assignment Dialog
        showAssignDialog?.let { item ->
            AssignmentDialog(
                item = item,
                users = users,
                onDismiss = { showAssignDialog = null },
                onDone = { assignments ->
                    val index = items.indexOf(item)
                    if (index >= 0) {
                        viewModel.assignItem(index, assignments)
                    }
                    showAssignDialog = null
                }
            )
        }
    }
}

@Composable
fun AssignmentCardPenitip(item: AssignableItem, onAssignClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(item.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = item.price,
                    onValueChange = {},
                    label = { Text("per unit", fontSize = 11.sp) },
                    prefix = { Text("Rp ") },
                    modifier = Modifier.weight(1f),
                    readOnly = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFFF9F9F9),
                        focusedContainerColor = Color(0xFFF9F9F9)
                    )
                )
                
                OutlinedTextField(
                    value = item.totalUnits.toString(),
                    onValueChange = {},
                    label = { Text("total units", fontSize = 11.sp) },
                    prefix = { Text("# ") },
                    modifier = Modifier.weight(0.7f),
                    readOnly = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFFF9F9F9),
                        focusedContainerColor = Color(0xFFF9F9F9)
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
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
                        if (item.isAssigned) "Terbagi: ${item.assignedCount}/${item.totalUnits} units"
                        else "Bagikan Item"
                    )
                }
            }
        }
    }
}
