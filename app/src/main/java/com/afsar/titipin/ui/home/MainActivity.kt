package com.afsar.titipin.ui.home

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.afsar.titipin.ui.buy.TitipanDetailActivity
import com.afsar.titipin.ui.home.screens.TitipankuScreen
import com.afsar.titipin.ui.circle.AddCircleActivity
import com.afsar.titipin.ui.circle.CircleDetailActivity


import com.afsar.titipin.ui.components.MainBottomNav
import com.afsar.titipin.ui.components.RouteScreen
import com.afsar.titipin.ui.home.screens.CircleScreen
import com.afsar.titipin.ui.home.screens.HomeScreen
import com.afsar.titipin.ui.home.screens.ProfileScreen
import com.afsar.titipin.ui.theme.TitipInTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TitipInTheme {
                val navController = rememberNavController()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = { MainBottomNav(navController = navController) }
                ) { innerPadding ->
                    AppNavHost(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = RouteScreen.Home.route,
        modifier = modifier
    ) {

        composable(RouteScreen.Home.route) {
            HomeScreen(
            )
        }

        composable(RouteScreen.Titip.route) { // Pastikan RouteScreen.Titip ada di file RouteScreen kamu
            TitipankuScreen(
                onSessionClick = { session ->
                    val intent = Intent(navController.context, TitipanDetailActivity::class.java)
                    intent.putExtra("EXTRA_SESSION", session)
                    navController.context.startActivity(intent)
                }
            )
        }

        composable(RouteScreen.Circles.route){
            CircleScreen(
                onAddCircleClick = {
                    val intent = Intent(navController.context, AddCircleActivity::class.java)
                    navController.context.startActivity(intent)
                },
                onCircleItemClick = { circle ->
                    val intent = Intent(navController.context, CircleDetailActivity::class.java)
                    intent.putExtra("EXTRA_CIRCLE_DATA", circle)
                    navController.context.startActivity(intent)
                }
            )
        }

        composable(RouteScreen.Profile.route) {
            ProfileScreen()
        }
    }
}