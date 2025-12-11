package com.example.titipin.ui.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.titipin.R
import com.example.titipin.ui.theme.PrimaryColor
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToWelcome: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val backgroundColor = if (isDark) Color(0xFFFFFFFF) else Color(0xFFFFFFFF)
    
    // Animasi offset Y (slide up)
    val offsetY by animateFloatAsState(
        targetValue = 0f,
        animationSpec = tween(
            durationMillis = 1000,
            delayMillis = 100,
            easing = FastOutSlowInEasing
        ),
        label = "offsetY"
    )
    // Initial value untuk slide up
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        started = true
    }
    val slideUpOffset = if (started) offsetY else 100f
    
    // Animasi alpha (fade in)
    val alpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(
            durationMillis = 800,
            delayMillis = 100,
            easing = FastOutSlowInEasing
        ),
        label = "alpha"
    )
    
    // Animasi scale
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(
            durationMillis = 1000,
            delayMillis = 100,
            easing = FastOutSlowInEasing
        ),
        label = "scale"
    )
    // Initial value untuk scale
    val initialScale = if (started) scale else 0.5f
    
    // Auto navigate setelah animasi selesai
    LaunchedEffect(Unit) {
        delay(2500) // Tunggu animasi selesai
        onNavigateToWelcome()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        // Logo tanpa background box
        Image(
            painter = painterResource(id = R.drawable.titipinlogo),
            contentDescription = "Logo Titip.in",
            modifier = Modifier
                .size((120 * initialScale).dp)
                .offset(y = slideUpOffset.dp)
                .alpha(alpha),
            contentScale = ContentScale.Fit
        )
    }
}
