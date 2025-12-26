package com.afsar.titipin.ui.components.organisms

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.afsar.titipin.ui.theme.OrangePrimary

@Composable
fun TitipinBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val NavyColor = Color(0xFF13204E)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, bottom = 38.dp)
    ) {
        Surface(
            color = Color.White,
            shadowElevation = 10.dp,
            shape = RoundedCornerShape(50.dp),
            modifier = Modifier.fillMaxWidth().height(64.dp)
        ) {
            NavigationBar(
                containerColor = Color.Transparent,
                tonalElevation = 0.dp,
                windowInsets = WindowInsets(0, 0, 0, 0),
                modifier = Modifier.fillMaxSize()
            ) {
                // --- ITEM 1: HOME ---
                val isHomeSelected = currentRoute == "home"
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home", modifier = Modifier.size(26.dp)) },
                    selected = isHomeSelected,
                    onClick = { onNavigate("home") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = OrangePrimary,
                        unselectedIconColor = NavyColor,
                        indicatorColor = Color.Transparent
                    ),
                    label = null
                )

                // --- ITEM 2: HISTORY ---

                val isHistorySelected = currentRoute == "history"
                NavigationBarItem(

                    icon = { Icon(Icons.Default.History, contentDescription = "History", modifier = Modifier.size(26.dp)) },
                    selected = isHistorySelected,

                    onClick = { onNavigate("history") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = OrangePrimary,
                        unselectedIconColor = NavyColor,
                        indicatorColor = Color.Transparent
                    ),
                    label = null
                )

                // --- ITEM 3: ADD ---
                val isAddSelected = currentRoute == "add"
                NavigationBarItem(
                    icon = {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add",
                            modifier = Modifier.size(32.dp)
                        )
                    },
                    selected = isAddSelected,
                    onClick = { onNavigate("add") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = OrangePrimary,
                        unselectedIconColor = NavyColor,
                        indicatorColor = Color.Transparent
                    ),
                    label = null
                )

                // --- ITEM 4: CIRCLE ---
                val isCircleSelected = currentRoute == "circle"
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Group, contentDescription = "Circle", modifier = Modifier.size(26.dp)) },
                    selected = isCircleSelected,
                    onClick = { onNavigate("circle") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = OrangePrimary,
                        unselectedIconColor = NavyColor,
                        indicatorColor = Color.Transparent
                    ),
                    label = null
                )

                // --- ITEM 5: PROFILE ---
                val isProfileSelected = currentRoute == "profile"
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile", modifier = Modifier.size(26.dp)) },
                    selected = isProfileSelected,
                    onClick = { onNavigate("profile") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = OrangePrimary,
                        unselectedIconColor = NavyColor,
                        indicatorColor = Color.Transparent
                    ),
                    label = null
                )
            }
        }
    }
}