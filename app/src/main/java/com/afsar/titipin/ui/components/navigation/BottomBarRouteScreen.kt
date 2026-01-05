package com.afsar.titipin.ui.components.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector

object RootRoutes {
    const val SPLASH = "splash_route"
    const val WELCOME = "welcome_route"
    const val LOGIN = "login_route"
    const val REGISTER = "register_route"
    const val MAIN_APP = "main_app_route"
}

sealed class BottomBarScreen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : BottomBarScreen("home_tab", "Home", Icons.Default.Home)
    object Session : BottomBarScreen("session_graph", "Titipan", Icons.Default.ShoppingCart)
    object Add : BottomBarScreen("add_graph", "Tambah", Icons.Default.Add)
    object Circles : BottomBarScreen("circles_graph", "Circle", Icons.Default.Groups)
    object Profile : BottomBarScreen("profile_tab", "Profil", Icons.Default.AccountCircle)

}

object DetailRoutes {
    const val CIRCLE_LIST = "circle_list_screen"
    const val CIRCLE_ADD = "circle_add_screen"
    const val CIRCLE_DETAIL = "circle_detail/{circleId}"
    fun createCircleDetailRoute(id: String) = "circle_detail/$id"
}
object ProfileRoutes {
    const val EDIT_PROFILE = "edit_profile_screen"
    const val PAYMENT_OPTION = "payment_option_screen"
}
object SessionRoutes {
    const val SESSION_LIST = "session_list_screen"
    const val SESSION_DETAIL = "session_detail/{sessionId}"
    const val SESSION_ADD = "session_add_screen"
    const val SHOPPING_LIST = "session_shopping_list/{sessionId}"

    // --- TAMBAHAN BARU ---
    const val PAYMENT_DETAIL = "payment_detail/{sessionId}"
    fun createPaymentRoute(id: String) = "payment_detail/$id"
    // ---------------------

    fun createSessionDetailRoute(id: String) = "session_detail/$id"
    fun createShoppingListRoute(id: String) = "session_shopping_list/$id"
}