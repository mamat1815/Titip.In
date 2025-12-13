package com.afsar.titipin.ui.components.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector

// HAPUS CLASS INI jika tidak dipakai di MainBottomNav.
// Lebih baik pakai BottomBarScreen saja biar tidak duplikat.
// sealed class RouteScreen(val route: String) { ... }

object RootRoutes {
    const val SPLASH = "splash_route"
    const val LOGIN = "login_route"
    const val REGISTER = "register_route" // Tambahkan ini biar rapi
    const val MAIN_APP = "main_app_route"
}

sealed class BottomBarScreen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : BottomBarScreen("home_tab", "Home", Icons.Default.Home)
    // Perbaikan: Samakan casing jadi lowercase semua biar aman
    object Session : BottomBarScreen("session_graph", "Titipan", Icons.Default.ShoppingCart)
    object Circles : BottomBarScreen("circles_graph", "Circle", Icons.Default.List)
    object Profile : BottomBarScreen("profile_tab", "Profil", Icons.Default.AccountCircle)
}

object DetailRoutes {
    const val CIRCLE_LIST = "circle_list_screen"
    const val CIRCLE_ADD = "circle_add_screen"
    const val CIRCLE_DETAIL = "circle_detail/{circleId}"
    fun createCircleDetailRoute(id: String) = "circle_detail/$id"
}

object SessionRoutes {
    const val SESSION_LIST = "session_list_screen"
    const val SESSION_DETAIL = "session_detail/{sessionId}"
    const val SESSION_ADD = "session_add_screen"

    const val SHOPPING_LIST = "session_shopping_list/{sessionId}"

    fun createSessionDetailRoute(id: String) = "session_detail/$id"
    fun createShoppingListRoute(id: String) = "session_shopping_list/$id"
}