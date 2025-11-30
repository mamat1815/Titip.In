package com.afsar.titipin.ui.circle

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.afsar.titipin.ui.theme.TitipInTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddCircleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TitipInTheme {
                AddCircleScreen(
                    onBackClick = {
                        val intent = Intent(this@AddCircleActivity, CircleActivity::class.java)
                        startActivity(intent)
                    }
                )

            }
        }
    }
}