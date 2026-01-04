package com.afsar.titipin.ui.home

import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.afsar.titipin.ui.buy.ShoppingListScreen
import com.afsar.titipin.ui.session.detail.SessionDetailScreen
//import com.afsar.titipin.ui.session.add.CreateSessionScreen
import com.afsar.titipin.ui.circle.add.AddCircleScreen
import com.afsar.titipin.ui.circle.detail.CircleDetailScreen
import com.afsar.titipin.ui.components.navigation.BottomBarScreen
import com.afsar.titipin.ui.components.navigation.DetailRoutes
import com.afsar.titipin.ui.components.navigation.MainBottomNav
import com.afsar.titipin.ui.components.navigation.SessionRoutes
import com.afsar.titipin.ui.home.screens.CircleScreen
import com.afsar.titipin.ui.home.screens.HomeScreen
import com.afsar.titipin.ui.home.screens.ProfileScreen
import com.afsar.titipin.ui.home.screens.SessionScreen
import com.afsar.titipin.ui.session.add.CreateSessionScreens
@Composable
fun MainAppNav(
    onLogOut: () -> Unit
){
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // REVISI: Hapus 'BottomBarScreen.Add.route' agar BottomBar hilang saat buat sesi
    val showBottomBar = currentRoute in listOf(
        BottomBarScreen.Home.route,
        SessionRoutes.SESSION_LIST,
         BottomBarScreen.Add.route, // <--- HAPUS INI (Disembunyikan biar fokus isi form)
        DetailRoutes.CIRCLE_LIST,
        BottomBarScreen.Profile.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                MainBottomNav(navController = navController)
            }
        },
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = BottomBarScreen.Home.route,
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
        ) {

            // ... (Home Tab & Session Tab TETAP SAMA) ...

            // 1. HOME TAB
            composable(BottomBarScreen.Home.route) {
                HomeScreen(
                    onSessionClick = { sessionId ->
                        navController.navigate(SessionRoutes.createSessionDetailRoute(sessionId))
                    }
                )
            }

            navigation(
                startDestination = SessionRoutes.SESSION_LIST,
                route = BottomBarScreen.Session.route
            ){
                composable(SessionRoutes.SESSION_LIST) {
                    SessionScreen(
                        onSessionClick = { sessionId ->
                            navController.navigate(SessionRoutes.createSessionDetailRoute(sessionId))
                        }
                    )
                }

                // ... detail & shopping list sama ...
                composable(
                    route = SessionRoutes.SESSION_DETAIL,
                    arguments = listOf(navArgument("sessionId"){type= NavType.StringType})
                ) {
                    SessionDetailScreen(
                        onBackClick = { navController.popBackStack() },
                        onGoToShoppingList = { sessionId ->
                            navController.navigate(SessionRoutes.createShoppingListRoute(sessionId))
                        }
                    )
                }

                composable(
                    route = SessionRoutes.SHOPPING_LIST,
                    arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
                ) {
                    ShoppingListScreen(
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }

            // 2. ADD TAB (Create Session)
            composable(BottomBarScreen.Add.route) {
                // Pastikan Manifest sudah ada <queries> biar Maps aman
                CreateSessionScreens(
                    onBackClick = {
                        navController.popBackStack()
                    },
                )
            }

            // 3. CIRCLE TAB (REVISI: Uncomment Callback)
            navigation(
                startDestination = DetailRoutes.CIRCLE_LIST,
                route = BottomBarScreen.Circles.route
            ) {
                composable(DetailRoutes.CIRCLE_LIST) {
                    CircleScreen(
                        // REVISI: Nyalakan kembali navigasi ini
//                        onAddCircleClick = {
//                            navController.navigate(DetailRoutes.CIRCLE_ADD)
//                        },
//                        onCircleItemClick = { circle ->
//                            navController.navigate(DetailRoutes.createCircleDetailRoute(circle.id))
//                        },
                    )
                }

                composable(DetailRoutes.CIRCLE_ADD) {
                    AddCircleScreen(
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(
                    route = DetailRoutes.CIRCLE_DETAIL,
                    arguments = listOf(navArgument("circleId") { type = NavType.StringType })
                ) {
                    CircleDetailScreen(
                        onBackClick = {
                            navController.popBackStack()
                        },
                        onSessionClick = { session ->
                            navController.navigate(SessionRoutes.createSessionDetailRoute(session.id))
                        }
                    )
                }
            }

            // 4. PROFILE TAB (REVISI: Uncomment Logout)
            composable(BottomBarScreen.Profile.route) {
                ProfileScreen(
                    // REVISI: Nyalakan fungsi Logout
//                    onLogoutClick = onLogOut,

                    // Jika fitur ini belum siap, boleh dikomentari.
                    // Tapi Logout wajib nyala.
//                    onCircleClick = {
//                        navController.navigate(BottomBarScreen.Circles.route) {
//                            popUpTo(navController.graph.startDestinationId) { saveState = true }
//                            launchSingleTop = true
//                            restoreState = true
//                        }
//                    }
                )
            }
        }
    }
}