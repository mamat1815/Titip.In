package com.afsar.titipin.ui.home

import HomeScreen
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.afsar.titipin.ui.buy.TitipanDetailActivity
import com.afsar.titipin.ui.buy.TitipankuScreen
import com.afsar.titipin.ui.circle.CircleActivity

// --- IMPORT SCREEN & KOMPONEN ---
// import com.afsar.titipin.ui.home.screens.ProfileScreen // Uncomment jika ProfileScreen sudah ada

import com.afsar.titipin.ui.components.MainBottomNav
import com.afsar.titipin.ui.components.RouteScreen
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
                    // Bottom Navigation Bar
                    bottomBar = { MainBottomNav(navController = navController) }
                ) { innerPadding ->
                    // Navigasi Utama Aplikasi
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

        // -----------------------------------------------------------
        // 1. MENU HOME
        // -----------------------------------------------------------
        composable(RouteScreen.Home.route) {
            HomeScreen(
                // Callback saat tombol "Circle Saya" diklik

            )
        }

        // -----------------------------------------------------------
        // 2. MENU TITIPANKU (LIST SESI JASTIP)
        // -----------------------------------------------------------
        composable(RouteScreen.Titip.route) { // Pastikan RouteScreen.Titip ada di file RouteScreen kamu
            TitipankuScreen(
                // Callback saat salah satu kartu sesi diklik
                onSessionClick = { session ->
                    // Pindah ke Activity Detail Titipan sambil bawa data sesi
                    val intent = Intent(navController.context, TitipanDetailActivity::class.java)
                    intent.putExtra("EXTRA_SESSION", session)
                    navController.context.startActivity(intent)
                }
            )
        }

        // -----------------------------------------------------------
        // 3. MENU PROFILE
        // -----------------------------------------------------------
        composable(RouteScreen.Profile.route) {
            // Jika ProfileScreen belum dibuat, pakai Text dummy dulu
            ProfileScreen()

        }
    }
}