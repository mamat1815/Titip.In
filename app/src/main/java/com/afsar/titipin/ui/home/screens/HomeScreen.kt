package com.afsar.titipin.ui.home.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.afsar.titipin.ui.components.CirclesImage
import com.afsar.titipin.ui.components.molecules.EmptySessionCard
import com.afsar.titipin.ui.components.molecules.OrdersItem
import com.afsar.titipin.ui.components.molecules.SessionCard
import com.afsar.titipin.ui.home.viewmodel.HomeViewModel
import com.afsar.titipin.ui.theme.BackgroundLight
import com.afsar.titipin.ui.theme.OrangePrimary
import com.afsar.titipin.ui.theme.TextPrimary
import com.afsar.titipin.ui.theme.TextSecondary


@Composable
fun HomeScreen(
    onSessionClick: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val currentUser = viewModel.currentUser
    val activeSession = viewModel.activeSessionState
    val myOrderSessionState = viewModel.myOrderSessionState
    val orderHistory = viewModel.orderHistory

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Surface(
                    color = Color.White,
                    shadowElevation = 4.dp,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            CirclesImage(
                                imageUrl = currentUser?.photoUrl,
                                size = 58.dp,
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Welcome back!", fontSize = 12.sp, color = TextSecondary)
                                Text(
                                    text = currentUser?.name ?: "Loading...",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                        }

                        Spacer(
                            Modifier.width(8.dp)
                        )

                        IconButton(onClick = {
//                            Todo navController.navigate(Screen.Notification.route)
                        },
                            modifier = Modifier.size(56.dp)) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notif",
                                tint = OrangePrimary
                            )
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }

                    if (activeSession.session == null) {
                        item { EmptySessionCard() }
                    } else {
                        item {
                            Box (
                                Modifier.clickable(
                                    onClick = { onSessionClick(activeSession.session.id) }

                                )
                            ){
                                SessionCard(
                                    session = activeSession.session,
                                    avatarUrls = activeSession.participantAvatars,
                                    currentUserId = currentUser?.uid
                                )
                            }
                        }


                    }

                    if (myOrderSessionState.session == null) {
                       item {
                           EmptySessionCard()
                       }
                    } else {
                        item {
                           Box(
                               Modifier.clickable(
                                   onClick = { onSessionClick(myOrderSessionState.session.id) }
                               )

                           ){
                               SessionCard(
                                   session = myOrderSessionState.session,
                                   avatarUrls = myOrderSessionState.participantAvatars,
                                   currentUserId = currentUser?.uid,
                                   customDescription = myOrderSessionState.orderItemName?.let { "Memesan: $it" }
                               )
                           }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp, bottom = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Riwayat Titipan",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            TextButton(onClick = {

                            }) {
                                Text("Lihat Semua", color = OrangePrimary, fontSize = 12.sp)
                            }
                        }
                    }

                    items(orderHistory) { order ->
                        OrdersItem(order)
                    }

                }
            }
        }

    }
}
