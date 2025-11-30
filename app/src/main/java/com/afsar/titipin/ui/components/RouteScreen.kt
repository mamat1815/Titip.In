package com.afsar.titipin.ui.components

sealed class RouteScreen(val route: String) {

    object Home: RouteScreen("home")
    object Circles: RouteScreen("circles")
    object ActiveSessions: RouteScreen("active_sessions")
    object History: RouteScreen("history")
}


