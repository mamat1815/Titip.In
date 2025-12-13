package com.afsar.titipin.ui.components.navigation


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.afsar.titipin.ui.home.MainAppNav
import com.afsar.titipin.ui.home.auth.login.LoginScreen
import com.afsar.titipin.ui.home.auth.login.LoginViewModel
import com.afsar.titipin.ui.home.auth.register.RegisterScreen

// Import LoginScreen Anda

@Composable
fun RootNavigation() {
    val navController = rememberNavController()
    val viewModel: LoginViewModel = hiltViewModel()
    val startDest by viewModel.startDestination.collectAsState()

    NavHost(
        navController = navController,
        startDestination = RootRoutes.SPLASH
    ) {
        composable(RootRoutes.SPLASH) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Titip.in Logo")
            }

            LaunchedEffect(startDest) {
                startDest?.let { target ->
                    navController.navigate(target) {
                        popUpTo(RootRoutes.SPLASH) { inclusive = true }
                    }
                }
            }
        }

        // 2. LOGIN SCREEN
        composable(RootRoutes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(RootRoutes.MAIN_APP) {
                        popUpTo(RootRoutes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate("register_route")
                }
            )
        }
        composable("register_route") {
            RegisterScreen(
//                onRegisterSuccess = {
//                    // Balik ke login atau langsung masuk app
//                    navController.popBackStack()
//                },
//                onBackToLogin = {
//                    navController.popBackStack()
//                }
            )
        }
        composable(RootRoutes.MAIN_APP) {
            MainAppNav(
                onLogOut = {
                    viewModel.logout()
                    navController.navigate(RootRoutes.LOGIN) {
                        popUpTo(RootRoutes.MAIN_APP) { inclusive = true }
                    }
                }
            )
        }
    }
}