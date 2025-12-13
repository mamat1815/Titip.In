package com.example.titipin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.titipin.ui.screen.*
import com.example.titipin.ui.theme.TitipinTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TitipinTheme {
                // 1. Buat NavController
                val navController = rememberNavController()

                // 2. Buat NavHost (Peta Navigasi)
                // Start dari SplashScreen
                NavHost(navController = navController, startDestination = "splash") {

                    // Rute Splash Screen (Animated)
                    composable("splash") {
                        SplashScreen(
                            onNavigateToWelcome = {
                                navController.navigate("welcome") {
                                    // Hapus splash screen dari back stack
                                    popUpTo("splash") { inclusive = true }
                                }
                            }
                        )
                    }

                    // Rute Welcome/Landing Screen
                    composable("welcome") {
                        WelcomeScreen(
                            onStartClick = {
                                // Navigate tanpa menghapus welcome dari stack
                                navController.navigate("sign_up")
                            },
                            onLoginClick = {
                                // Navigate tanpa menghapus welcome dari stack
                                navController.navigate("sign_in")
                            }
                        )
                    }

                    // Rute Sign Up (Register)
                    composable("sign_up") {
                        SignUpScreen(
                            onSignUpClick = {
                                navController.navigate("home") {
                                    popUpTo("welcome") { inclusive = true }
                                }
                            },
                            onSignInClick = {
                                navController.navigate("sign_in") {
                                    popUpTo("sign_up") { inclusive = true }
                                }
                            },
                            onGoogleSignInClick = {
                                navController.navigate("home") {
                                    popUpTo("welcome") { inclusive = true }
                                }
                            },
                            onBackClick = {
                                // Navigate up ke previous screen (Welcome)
                                navController.navigateUp()
                            }
                        )
                    }

                    // Rute Sign In (Login)
                    composable("sign_in") {
                        SignInScreen(
                            onSignInClick = {
                                navController.navigate("home") {
                                    popUpTo("welcome") { inclusive = true }
                                }
                            },
                            onSignUpClick = {
                                navController.navigate("sign_up") {
                                    popUpTo("sign_in") { inclusive = true }
                                }
                            },
                            onGoogleSignInClick = {
                                navController.navigate("home") {
                                    popUpTo("welcome") { inclusive = true }
                                }
                            },
                            onBackClick = {
                                // Navigate up ke previous screen (Welcome)
                                navController.navigateUp()
                            }
                        )
                    }

                    // Rute Halaman Utama (Beranda)
                    composable("home") {
                        HomeScreen(
                            onNavigateToCreate = {
                                navController.navigate("create_session")
                            },
                            onNavigateToTitipanku = {
                                navController.navigate("titipanku") {
                                    popUpTo("home") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            onNavigateToProfile = {
                                navController.navigate("profile") {
                                    popUpTo("home") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            onNavigateToHistory = {
                                navController.navigate("history")
                            },
                            onNavigateToRequest = {
                                navController.navigate("request_item")
                            }
                        )
                    }

                    // Rute Request Item (Penitip)
                    composable("request_item") {
                        RequestItemScreen(
                            onBackClick = { navController.navigateUp() },
                            onSubmitClick = {
                                navController.navigate("titipanku") {
                                    popUpTo("home") { saveState = true }
                                }
                            }
                        )
                    }

                    // Rute Titipanku Screen
                    composable("titipanku") {
                        TitipankuScreen(
                            onNavigateToHome = {
                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = true }
                                }
                            },
                            onNavigateToProfile = {
                                navController.navigate("profile") {
                                    popUpTo("titipanku") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            onNavigateToInProgress = {
                                navController.navigate("in_progress")
                            }
                        )
                    }

                    // Rute In Progress (Penitip POV)
                    composable("in_progress") {
                        InProgressScreen(
                            onBackClick = { navController.navigateUp() },
                            onChatClick = { navController.navigate("session_chat") },
                            onAutoNavigateToAssignment = {
                                navController.navigate("penitip_live_assignment")
                            }
                        )
                    }

                    // Rute Penitip Live Assignment
                    composable("penitip_live_assignment") {
                        PenitipLiveAssignmentScreen(
                            onBackClick = { navController.navigateUp() },
                            onChatClick = { navController.navigate("session_chat") },
                            onAllAssigned = {
                                navController.navigate("penitip_payment")
                            }
                        )
                    }

                    // Rute Penitip Payment
                    composable("penitip_payment") {
                        PenitipPaymentScreen(
                            onBackClick = { navController.navigateUp() },
                            onChatClick = { navController.navigate("session_chat") }
                        )
                    }

                    // Rute Profile Screen
                    composable("profile") {
                        ProfileScreen(
                            onNavigateToHome = {
                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = true }
                                }
                            },
                            onNavigateToTitipanku = {
                                navController.navigate("titipanku") {
                                    popUpTo("profile") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            onNavigateToHistory = {
                                navController.navigate("history")
                            }
                        )
                    }

                    // Rute Halaman Buat Sesi
                    composable("create_session") {
                        CreateSessionScreen(
                            onBackClick = {
                                navController.popBackStack() // Kembali ke halaman sebelumnya
                            },
                            onSessionCreated = {
                                navController.navigate("session_detail") {
                                    popUpTo("home") { inclusive = false }
                                }
                            }
                        )
                    }
                    
                    // Rute Detail Sesi
                    composable("session_detail") {
                        SessionDetailScreen(
                            onBackClick = {
                                navController.navigate("create_session") {
                                    popUpTo("session_detail") { inclusive = true }
                                }
                            },
                            onNavigateToHome = {
                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = true }
                                }
                            },
                            onNavigateToTitipanku = {
                                navController.navigate("titipanku") {
                                    popUpTo("session_detail") { inclusive = true }
                                }
                            },
                            onNavigateToProfile = {
                                navController.navigate("profile") {
                                    popUpTo("session_detail") { inclusive = true }
                                }
                            },
                            onNavigateToShopping = {
                                navController.navigate("shopping_list")
                            }
                        )
                    }
                    
                    // Rute Shopping List
                    composable("shopping_list") {
                        ShoppingListScreen(
                            onBackClick = {
                                navController.navigateUp()
                            },
                            onChatClick = {
                                navController.navigate("session_chat")
                            },
                            onUploadClick = {
                                navController.navigate("upload_receipt")
                            },
                            onNavigateToHome = {
                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = true }
                                }
                            },
                            onNavigateToTitipanku = {
                                navController.navigate("titipanku") {
                                    popUpTo("shopping_list") { inclusive = true }
                                }
                            },
                            onNavigateToProfile = {
                                navController.navigate("profile") {
                                    popUpTo("shopping_list") { inclusive = true }
                                }
                            }
                        )
                    }
                    
                    // Rute Session Chat
                    composable("session_chat") {
                        SessionChatScreen(
                            onBackClick = {
                                navController.navigateUp()
                            }
                        )
                    }
                    
                    // Rute Upload Receipt
                    composable("upload_receipt") {
                        UploadReceiptScreen(
                            onBackClick = {
                                navController.navigateUp()
                            },
                            onScanReceiptClick = {
                                navController.navigate("receipt_scanner")
                            },
                            onChatClick = {
                                navController.navigate("session_chat")
                            },
                            onContinueToPayment = {
                                navController.navigate("payment_delivery")
                            }
                        )
                    }
                    
                    // Rute Receipt Scanner
                    composable("receipt_scanner") {
                        ReceiptScannerScreen(
                            onBackClick = {
                                navController.navigateUp()
                            },
                            onCaptureClick = {
                                navController.navigateUp()
                            }
                        )
                    }
                    
                    // Rute Item Assignment
                    composable("item_assignment") {
                        ItemAssignmentScreen(
                            onBackClick = {
                                navController.navigateUp()
                            },
                            onNavigateToPayment = {
                                navController.navigate("payment_delivery") {
                                    popUpTo("item_assignment") { inclusive = true }
                                }
                            }
                        )
                    }

                    // Rute Penitip Status
                    composable("penitip_status") {
                        PenitipStatusScreen(
                            onBackClick = { navController.navigateUp() }
                        )
                    }
                    
                    // Rute Payment & Delivery
                    composable("payment_delivery") {
                        PaymentDeliveryScreen(
                            onBackClick = {
                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = true }
                                }
                            },
                            onChatClick = {
                                navController.navigate("session_chat")
                            },
                            onFinishOrder = {
                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = true }
                                }
                            }
                        )
                    }
                    
                    // Rute History
                    composable("history") {
                        HistoryScreen(
                            onBackClick = {
                                navController.navigateUp()
                            }
                        )
                    }
                }
            }
        }
    }
}