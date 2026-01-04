package com.afsar.titipin.ui.home.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.afsar.titipin.data.model.Session
//import com.afsar.titipin.ui.components.molecules.HistoryItem
import com.afsar.titipin.ui.components.molecules.OrdersItem
import com.afsar.titipin.ui.components.molecules.SessionsItem
import com.afsar.titipin.ui.home.viewmodel.SessionViewModel

//
//@Composable
//fun SessionScreen(
////    navController: NavController, // <--- 1. Tambahkan ini untuk navigasi
//    viewModel: SessionViewModel = hiltViewModel()
//) {
////    val titipListState by viewModel.titipList.collectAsState()
////    val sesiListState by viewModel.sesiList.collectAsState()
//    val session = viewModel.mySessions
//    val orders = viewModel.myOrders
//
//    var selectedTabIndex by remember { mutableIntStateOf(0) }
//    val tabs = listOf("Titip", "Sesi")
//
//    // 2. Gunakan Box sebagai wadah utama agar BottomBar bisa melayang (overlay)
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color.White)
//    ) {
//        Scaffold(
//            containerColor = Color.Transparent, // Transparan agar ikut warna Box
//            topBar = {
//                Column(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .background(Color.White)
//                ) {
//                    Text(
//                        text = "Riwayat",
//                        fontSize = 24.sp,
//                        fontWeight = FontWeight.Bold,
//                        color = Color.Black,
//                        modifier = Modifier.padding(start = 16.dp, top = 40.dp, bottom = 8.dp)
//                    )
//
//                    TabRow(
//                        selectedTabIndex = selectedTabIndex,
//                        containerColor = Color.White,
//                        contentColor = Color.Black,
//                        indicator = { tabPositions ->
//                            TabRowDefaults.SecondaryIndicator(
//                                Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
//                                color = Color(0xFFFF8C42)
//                            )
//                        }
//                    ) {
//                        tabs.forEachIndexed { index, title ->
//                            Tab(
//                                selected = selectedTabIndex == index,
//                                onClick = { selectedTabIndex = index },
//                                text = {
//                                    Text(
//                                        text = title,
//                                        fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
//                                        color = if (selectedTabIndex == index) Color(0xFFFF8C42) else Color.Gray
//                                    )
//                                }
//                            )
//                        }
//                    }
//                }
//            }
//        ) { paddingValues ->
//            Column(modifier = Modifier.padding(paddingValues)) {
//                val currentList = if (selectedTabIndex == 0) session else orders
//
//                LazyColumn(
//                    // 3. Tambahkan padding bottom besar (misal 100.dp) agar list paling bawah tidak ketutup BottomBar
//                    contentPadding = PaddingValues(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 100.dp),
//                    verticalArrangement = Arrangement.spacedBy(8.dp)
//                ) {
//                    if (currentList == session) {
//                        items(session) { history ->
//                            SessionsItem(history = history)
//                        }
//                    } else {
//                        items(orders) { order ->
//                            OrdersItem(history = order)
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
@Composable
fun SessionScreen(
    // navController: NavController, // Uncomment jika butuh navigasi detail
    onSessionClick: (String) -> Unit,
    viewModel: SessionViewModel = hiltViewModel()
) {
    // Ambil state dari ViewModel
    val sessionList = viewModel.mySessions
    val orderList = viewModel.myOrders

    // Trigger load data setiap kali layar ini dibuka
    LaunchedEffect(Unit) {
        viewModel.loadMySessions()
        viewModel.loadMyOrders()
//        viewModel.loadSessionDetail(se)
    }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Titip (Guest)", "Sesi (Host)") // Ubah label biar jelas

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Column(modifier = Modifier.background(Color.White)) {
                    Text(
                        text = "Riwayat",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(start = 16.dp, top = 40.dp, bottom = 8.dp)
                    )

                    TabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = Color.White,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                color = Color(0xFFFF8C42)
                            )
                        }
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index },
                                text = {
                                    Text(
                                        text = title,
                                        fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedTabIndex == index) Color(0xFFFF8C42) else Color.Gray
                                    )
                                }
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->

            // Logic Tab Switching
            LazyColumn(
                contentPadding = PaddingValues(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(paddingValues)
            ) {
                if (selectedTabIndex == 0) {
                    // TAB 0: ORDER LIST (Titip)
                    if (orderList.isEmpty()) {
                        item { EmptyState("Belum ada titipan berjalan/selesai.") }
                    } else {
                        items(orderList) { order ->
                            Box(
                                modifier = Modifier.clickable {
                                    onSessionClick(order.sessionId)
                                }
                            ){
                                OrdersItem(history = order)
                            }
                        }
                    }
                } else {
                    // TAB 1: SESSION LIST (Sesi)
                    if (sessionList.isEmpty()) {
                        item { EmptyState("Kamu belum pernah membuka sesi jastip.") }
                    } else {
                        items(sessionList) { session ->
                            Box(
                                modifier = Modifier.clickable {
                                    onSessionClick(session.id)
                                }
                            ){
                                SessionsItem(history = session)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Komponen Pembantu UI Kosong
@Composable
fun EmptyState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 50.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = message, color = Color.Gray)
    }
}

@Composable
fun SessionScreens(
    onSessionItemClick: (Session) -> Unit,
    viewModel: SessionViewModel = hiltViewModel()
) {
    // --- PERBAIKAN: Load data saat screen dibuka ---
    LaunchedEffect(Unit) {
        viewModel.loadMySessions()
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("Titipanku (Riwayat)", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            if (viewModel.mySessions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Belum ada riwayat titipan.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(viewModel.mySessions) { session ->
                        SessionItemCard(session, onClick = { onSessionItemClick(session) })
                    }
                }
            }
        }
    }
}

@Composable
fun SessionItemCard(session: Session, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Status Visual
            val iconColor = if (session.status == "open") Color(0xFF00C853) else Color.Gray

            Icon(
                Icons.Default.ShoppingBag,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(session.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = if (session.status == "open") Color(0xFFE8F5E9) else Color(0xFFF5F5F5),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = if (session.status == "open") "Berlangsung" else "Selesai",
                            color = if (session.status == "open") Color(0xFF2E7D32) else Color.Gray,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}