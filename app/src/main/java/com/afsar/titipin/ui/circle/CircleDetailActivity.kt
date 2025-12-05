package com.afsar.titipin.ui.circle


import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.afsar.titipin.data.model.Circle
import com.afsar.titipin.ui.buy.TitipanDetailActivity
import com.afsar.titipin.ui.theme.TitipInTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CircleDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Ambil Data Parcelable dari Intent
        val circleData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("EXTRA_CIRCLE_DATA", Circle::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("EXTRA_CIRCLE_DATA")
        }

        setContent {
            TitipInTheme {
                if (circleData != null) {
                    // 2. Tampilkan Screen Detail
                    CircleDetailScreen(
                        onBackClick = { finish() } ,// Kembali ke list,
                                onSessionClick = { session ->
                            // Pindah ke Activity Detail Titipan (yang menampilkan timer, order, chat)
                            val intent = Intent(this, TitipanDetailActivity::class.java)
                            intent.putExtra("EXTRA_SESSION", session)
                            startActivity(intent)
                        }
                    )
                } else {
                    // Fallback jika data error/kosong
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Data Circle tidak ditemukan")
                    }
                }
            }
        }
    }
}