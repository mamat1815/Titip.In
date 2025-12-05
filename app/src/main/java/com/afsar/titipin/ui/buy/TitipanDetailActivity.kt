package com.afsar.titipin.ui.buy

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.*
import com.afsar.titipin.data.model.JastipSession
import com.afsar.titipin.ui.theme.TitipInTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TitipanDetailActivity : ComponentActivity() {

    // Kita init ViewModel di level Activity agar bisa dipakai bersama
    // oleh DetailScreen dan ShoppingListScreen (Sharing Data Timer/Chat)
    private val viewModel: TitipankuViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Ambil Data Session dari Intent
        val session = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("EXTRA_SESSION", JastipSession::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("EXTRA_SESSION")
        }

        // 2. Load Data ke ViewModel (Sekali saja saat Activity dibuat)
        if (session != null && viewModel.currentSession == null) {
            viewModel.loadSessionDetail(session)
        }

        setContent {
            TitipInTheme {
                // State lokal untuk mengatur halaman mana yang tampil
                // "detail" atau "shopping"
                var currentScreen by remember { mutableStateOf("detail") }

                if (session != null) {
                    if (currentScreen == "detail") {
                        // --- HALAMAN 1: DETAIL SESI ---
                        SessionDetailScreen(
                            session = session,
                            onBackClick = { finish() },
                            onGoToShoppingList = { currentScreen = "shopping" }, // Pindah ke Belanja
                            viewModel = viewModel // Pass ViewModel yang sama
                        )
                    } else {
                        // --- HALAMAN 2: DAFTAR BELANJA ---
                        ShoppingListScreen(
                            onBackClick = { currentScreen = "detail" }, // Balik ke Detail
                            viewModel = viewModel // Pass ViewModel yang sama
                        )
                    }
                }
            }
        }
    }
}