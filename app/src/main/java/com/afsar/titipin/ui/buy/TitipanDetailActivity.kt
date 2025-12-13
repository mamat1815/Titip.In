package com.afsar.titipin.ui.buy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TitipanDetailActivity : ComponentActivity() {

//    private val viewModel: SessionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

//        val session = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            intent.getParcelableExtra("EXTRA_SESSION", Session::class.java)
//        } else {
//            @Suppress("DEPRECATION")
//            intent.getParcelableExtra("EXTRA_SESSION")
//        }
//
//        if (session != null && viewModel.currentSession == null) {
//            viewModel.loadSessionDetail(session)
//        }
//
//        setContent {
//            TitipInTheme {
//                var currentScreen by remember { mutableStateOf("detail") }
//
//                if (session != null) {
//                    if (currentScreen == "detail") {
//                        SessionDetailScreen(
//                            session = session,
//                            onBackClick = { finish() },
//                            onGoToShoppingList = {
//                                currentScreen = "shopping"
//                            }, // Pindah ke Belanja
//                            viewModel = viewModel // Pass ViewModel yang sama
//                        )
//                    } else {
//                        // --- HALAMAN 2: DAFTAR BELANJA ---
//                        ShoppingListScreen(
//                            onBackClick = { currentScreen = "detail" }, // Balik ke Detail
//                            viewModel = viewModel // Pass ViewModel yang sama
//                        )
//                    }
//                }
//            }
//        }
    }
}