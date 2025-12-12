package com.afsar.titipin.ui.circle

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.afsar.titipin.ui.home.screens.CircleScreen
import com.afsar.titipin.ui.theme.TitipInTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CircleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TitipInTheme {

                CircleScreen(
                    onAddCircleClick = {
                        val intent = Intent(this@CircleActivity, AddCircleActivity::class.java)
                        startActivity(intent)
                    },
                    onCircleItemClick = { circle ->
                        val intent = Intent(this@CircleActivity, CircleDetailActivity::class.java)
                        // Kirim Object Circle ke Activity sebelah
                        intent.putExtra("EXTRA_CIRCLE_DATA", circle)
                        startActivity(intent)
                    }
                )
            }
        }

    }
}