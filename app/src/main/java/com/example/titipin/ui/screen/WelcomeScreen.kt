package com.example.titipin.ui.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.titipin.R
import com.example.titipin.ui.theme.PrimaryColor
import com.example.titipin.ui.theme.TextPrimary
import com.example.titipin.ui.theme.TextSecondary
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun WelcomeScreen(
    onStartClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    
    val backgroundColor = if (isDark) Color(0xFF121212) else Color(0xFFFFFFFF)
    val textPrimaryColor = if (isDark) Color.White else Color(0xFF111827)
    val textSecondaryColor = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)
    val circleColor = if (isDark) Color(0xFF4B5563) else Color(0xFF9CA3AF)
    
    // State untuk animasi circular ripple
    var isExpanding by remember { mutableStateOf(false) }
    var rippleCenter by remember { mutableStateOf(Offset.Zero) }
    
    // Animasi circular scale - seperti air menyebar
    val rippleScale by animateFloatAsState(
        targetValue = if (isExpanding) 20f else 0f, // Scale besar untuk cover screen
        animationSpec = tween(
            durationMillis = 2500,
            easing = FastOutSlowInEasing
        ),
        label = "rippleExpand",
        finishedListener = {
            if (isExpanding) {
                onStartClick()
            }
        }
    )
    
    // Root Box TANPA padding untuk overlay
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // Content Box DENGAN padding
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Spacer untuk memberi ruang di atas
                Spacer(modifier = Modifier.weight(0.1f))
                
                // Bagian Tengah - Concentric Circles dengan Avatar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.4f),
                    contentAlignment = Alignment.Center
                ) {
                    ConcentricCirclesWithAvatars(circleColor)
                }

                Spacer(modifier = Modifier.height(60.dp))
                
                // Bagian Bawah - Teks dan Tombol
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.37f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Selamat Datang di Titip.in",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimaryColor,
                        textAlign = TextAlign.Center,
                        lineHeight = 40.sp
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "Layanan titip barang dan makanan dengan sesi terbatas waktu. Bayar mudah dengan QR atau cash.",
                        fontSize = 16.sp,
                        color = textSecondaryColor,
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(40.dp))
                    
                    // Swipe to Start Button
                    SwipeToStartButton(
                        onSwipeComplete = { centerPosition ->
                            rippleCenter = centerPosition
                            isExpanding = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(30.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sudah punya akun? ",
                            fontSize = 14.sp,
                            color = textSecondaryColor
                        )
                        TextButton(
                            onClick = onLoginClick,
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.offset(y = (-2).dp)
                        ) {
                            Text(
                                text = "Masuk",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryColor
                            )
                        }
                    }
                }
            }
        }
        
        // Circular Ripple Overlay - White CIRCLE yang membesar
        if (rippleScale > 0f) {
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                // Calculate center position dalam pixel
                val centerX = size.width * rippleCenter.x
                val centerY = size.height * rippleCenter.y
                
                // Calculate radius maksimal untuk cover entire screen
                // Gunakan diagonal screen sebagai max radius
                val maxRadius = kotlin.math.sqrt(
                    (size.width * size.width + size.height * size.height).toDouble()
                ).toFloat()
                
                // Current radius berdasarkan animation progress
                val currentRadius = maxRadius * (rippleScale / 20f)
                
                // Draw white circle
                drawCircle(
                    color = Color.White,
                    radius = currentRadius,
                    center = androidx.compose.ui.geometry.Offset(centerX, centerY)
                )
            }
        }
    }
}

@Composable
fun ConcentricCirclesWithAvatars(circleColor: Color) {
    val circleSize = 320.dp
    val innerCircleSize = 224.dp // 70% dari 320
    
    // Infinite rotation animation untuk orbit avatar
    val infiniteTransition = rememberInfiniteTransition(label = "rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 30000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    Box(
        modifier = Modifier.size(circleSize),
        contentAlignment = Alignment.Center
    ) {
        // Lingkaran Luar (Dashed Border)
        Box(
            modifier = Modifier
                .size(circleSize)
                .dashedBorder(
                    color = circleColor.copy(alpha = 0.3f),
                    strokeWidth = 1.dp,
                    dashLength = 8.dp,
                    gapLength = 8.dp
                )
        )
        
        // Lingkaran Dalam (Dashed Border)
        Box(
            modifier = Modifier
                .size(innerCircleSize)
                .dashedBorder(
                    color = circleColor.copy(alpha = 0.3f),
                    strokeWidth = 1.dp,
                    dashLength = 8.dp,
                    gapLength = 8.dp
                )
        )
        
        // Avatar di Lingkaran Luar - BERGERAK MENGELILINGI
        AvatarAtPosition(0, 15f + rotation, 160f)  // orbit avatar-1
        AvatarAtPosition(1, 55f + rotation, 160f)  // orbit avatar-2
        AvatarAtPosition(2, 95f + rotation, 160f)  // orbit avatar-3
        AvatarAtPosition(3, 135f + rotation, 160f) // orbit avatar-4
        AvatarAtPosition(4, 290f + rotation, 160f) // orbit avatar-5
        
        // Avatar di Lingkaran Dalam - BERGERAK MENGELILINGI
        AvatarAtPosition(5, 30f + rotation, 112f)  // orbit avatar-6
        AvatarAtPosition(6, 120f + rotation, 112f) // orbit avatar-7
        AvatarAtPosition(7, 210f + rotation, 112f) // orbit avatar-8
        AvatarAtPosition(8, 300f + rotation, 112f) // orbit avatar-9
        
        // Logo di Tengah (di atas avatar yang berputar)
        Image(
            painter = painterResource(id = R.drawable.titipinlogo),
            contentDescription = "Logo Titip.in",
            modifier = Modifier.size(80.dp),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
fun AvatarAtPosition(index: Int, angleDegrees: Float, radius: Float) {
    val avatarUrls = listOf(
        "https://lh3.googleusercontent.com/aida-public/AB6AXuD16ROzPOey9_P69NiANpG27E6Hjp7ZZGhuCuh91aTYopp7JKd0OIHZkYIrXCyYhtbPjeKn3By5vRTU62rQT0PAGMb7uwFF_uToPEV2lsl6GxYGItPYY9mLlbRqLESQNjYNfPHmsYn0QtVhcQLzArc7pIpgJCTgCuktqzOI7mIEuSZ2ZouCas3PDvuKbrwcOLGsznQokB-AmvBMupi9CD0J45nKQ2ReQJrP-FxFp0vhEUALcVK-HuVa739wrr3rGYSTtkUygMw6LA4",
        "https://lh3.googleusercontent.com/aida-public/AB6AXuC6HbYWHPmX-q2yaxA8f-AY_lSM5T0YMiSwMtp8xTc8uLYb6btYrQN8YhDFlXZvCJ-fqX6VcNoZZm2UKqkLUBNDnuEnXmIyzmP1S-jKl4DK76wpWHZY3eG1HgF-lvRAqql-Ft7W0WHN6hdRIk-Ga9M11PbucXyTVqpy4athtw3kQOjndFnLqSyKgMPwvdRbO7e0rdc370RFRWcLykyMiO8WeoV6ZodVIh9Zej4WYCYcNQSg_mTq7XtiwY9slWt8amOcboSVkq98SPs",
        "https://lh3.googleusercontent.com/aida-public/AB6AXuCwEs56WWw-wDQnRiHeaVtA1p-MzjXHqkwXSTAsuEsJujEyoNVEwzmFoE388vbM4KvlU-NzBgMYhvosya4sGs3PnI3Dd-Zkx3kiKnJVpyd7_e5aUtsrm3PxpupxJkngVKhKNwEvHhBNdXXciEHBxjVPzeZMGImY_z5G70VrXqZws717stjGpf1VATOkZxq6Fw1c52ItfZPC1XyFN9aQjpl9I_3JcWaZgjBcnfRhr2oREZ5ZrspQ2N7T2PD8hjm9STP2_nfVYd_kKuE",
        "https://lh3.googleusercontent.com/aida-public/AB6AXuAslSzNOqJl_cD4DpC2wAaY55SG806REsu1gFLATSeI_QUcu6P41kRwMukcc1I3cvcVOzkLmcXIRc48DysszPVpDlHyT3H9M5sYVhVREsRjaIV-ceQl71ofNOTtlb6HUGD30mNOdS6C1APgkWeVo-Bv-KdQnmMtKA5njc3CqwyIgLE9XnCkWfm3HWSqQqhGXm8lG1YVeO2dU0PZYBpUphD46bjUnRldNAErZjlDR1Mnue6nhOVIHyoEUi3QwjEb18a51uJUolJsfRM",
        "https://lh3.googleusercontent.com/aida-public/AB6AXuBIopvUdDe281ZxugwHwZCRDZKS_zwVwtFUlczT277IlLlYB_wclHSNtDjsMQehwmOYdl3VA4kf38HLK-HnqlJp_shWJrstyCOgUvn0aGITW0LSC-KJ5sN8nSK0mik6wYb8pcBDdcaqJg9Rv0yoGJqMwIINDj0XXyexl4vhi79CYEBz5wQFnJGQXEEdXZczV1yLnwFjCN_BBgiYDkZsoKytsTBJJ2qGWWcAvJJi8QWcbivuNFR9z1d87pMYR9kIK7-wfx_OneyEiKg",
        "https://lh3.googleusercontent.com/aida-public/AB6AXuBYyHyxgaavm3RVPRLlKXgqls-plzukNJ3ZhDJom1Mf9XDNpqwpZUMTij0N-T324pQ_Ki_RRAg7ykszylYRJ6wHq_ucCmvAiJARBQTn11u2O4x4gmKjHYLV1w7mjrfAFtVq8esWz5_AyHvDkVAhtB2eD9oX_TBPkFeSpwTRqUXYrI2mO1r5PdjrLKv6NU_YOHqC1opyOKX5YiRNidMzWxIEgp3lMEzrWbuSxN5jt785j5xFhzbz3pO400hdZh3A0TvlGBW3P-7y-Gk",
        "https://lh3.googleusercontent.com/aida-public/AB6AXuC11HFFiehgb5MarhCoXgM-jjoor2AuYpYXWn0xmzc37CjnkofntIuY0L-FZPOjdZehB1739U_XlQoAtOUT8sqAqUn3sdWLDYnvDyTOjMV1ihLhlqG0NQrafHfKYq96uH1gC5eN2AjWZLz3YMcyB8VTohH6QvrGYMjUJHgt-UKhwCsL-suLBeP-qZGiaBqQVlZlABvEZTeNoBU5beHAbtkyKZEmu-xQnuUQY0RM6lODJ2eNV5kDjyobX5BSoQYMDc9bSo0VGZhCqmM",
        "https://lh3.googleusercontent.com/aida-public/AB6AXuCm79UDndxXOnjyTXzIM7kXeL9-w3EI97oi5IjRuO_guWglkBwaPHniF5IcAfPVDcL0cH5B4nKi2aXL0Bxxs55U9Wh8QBqhGybfRVruKbHp3puRfvBRECHLEaJxgAYaUTefJiw_B64NsfZU45tnleNPlYsZ9BMH_rtUhUP-LBqe8q-S8VU4uFv9BgtOqOFPeZaoVhhhlSEeIX0dtr0lMQsNYOzSiL2z0iY7z_-gXVNLCb5QS9cKDFi2Y6-8UsIehvGjzMGkEWeg19A",
        "https://lh3.googleusercontent.com/aida-public/AB6AXuDxlNETiyWHlDRoagSPMrIvRKYOVdXHNW2sRFWNjY8TgxtnSpn61zFd7mKPvA46lMQEWXSOTFtztzP0oodai9RIn52X7fbWzbnMhymN2seaHNdC1k44F-3968--vapNEJ0HlhcMU0uYh90k9o2QY5i969icQ-rtNjahX6T6CKJR2vufTs3nMA7GK8c399Cwnk13igaQihWz276Dh5lhY_aj3a3S_25fxonyIOeE3Da9yPb4zRW63nonBjrGhdhgvDsDSG_65QlQ9aM"
    )
    
    val isDark = isSystemInDarkTheme()
    val borderColor = if (isDark) Color(0xFF1E1E1E) else Color.White
    
    // Konversi derajat ke radian untuk positioning
    val angleRadians = Math.toRadians(angleDegrees.toDouble())
    
    // Hitung posisi X dan Y
    val xOffset = (cos(angleRadians) * radius).dp
    val yOffset = (sin(angleRadians) * radius).dp
    
    Box(
        modifier = Modifier
            .offset(x = xOffset, y = yOffset)
            .size(36.dp)
            .clip(CircleShape)
            .border(2.dp, borderColor, CircleShape)
            .background(Color(0xFFE0E0E0))
    ) {
        AsyncImage(
            model = avatarUrls.getOrNull(index) ?: "",
            contentDescription = "User Avatar",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

// Extension function untuk membuat dashed border
@Composable
fun Modifier.dashedBorder(
    color: Color,
    strokeWidth: Dp,
    dashLength: Dp,
    gapLength: Dp
) = this.then(
    Modifier
        .border(
            width = strokeWidth,
            color = color,
            shape = CircleShape
        )
)

// Swipe to Start Button Composable
@Composable
fun SwipeToStartButton(
    onSwipeComplete: (Offset) -> Unit, // Pass center position untuk ripple
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val textColor = if (isDark) Color.White else Color.White
    
    BoxWithConstraints(
        modifier = modifier
            .height(64.dp) // Slightly taller
            .clip(RoundedCornerShape(32.dp))
            .background(PrimaryColor) // Background primary color
    ) {
        // Konversi unit menggunakan LocalDensity
        val density = LocalDensity.current
        val widthPx = with(density) { maxWidth.toPx() }
        val containerHeight = with(density) { 64.dp.toPx() }
        
        // Button size relative to container height
        val buttonSize = 56.dp
        val buttonSizePx = with(density) { buttonSize.toPx() }
        val padding = 4.dp
        val paddingPx = with(density) { padding.toPx() }

        // State drag dalam Pixel (float)
        var offsetX by remember { mutableFloatStateOf(0f) }

        // Lebar track efektif yang bisa digeser
        val maxOffsetPx = widthPx - buttonSizePx - (paddingPx * 2)

        // Threshold activation
        val activationThresholdPx = maxOffsetPx * 0.9f

        Box(
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier.fillMaxSize()
        ) {
            // Background text with Fade Out logic
            val progress = (offsetX / maxOffsetPx).coerceIn(0f, 1f)
            val alpha = (1f - progress).coerceIn(0f, 1f)

            if (alpha > 0) {
                Text(
                    text = "Slide untuk Mulai",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = textColor.copy(alpha = alpha),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 120.dp),
                    textAlign = TextAlign.Start
                )
            }

            // Draggable Slider Button (Putih)
            Box(
                modifier = Modifier
                    .offset(x = with(density) { offsetX.toDp() })
                    .padding(padding)
                    .size(buttonSize)
                    .clip(CircleShape)
                    .background(Color.White) // Slider putih
                    .pointerInput(maxOffsetPx) {
                        detectDragGestures(
                            onDragEnd = {
                                if (offsetX >= activationThresholdPx) {
                                    // Calculate normalized center position (0-1)
                                    val sliderCenterX = (offsetX + buttonSizePx / 2) / widthPx
                                    val sliderCenterY = 0.85f // Approximate Y position (button di bawah)
                                    
                                    onSwipeComplete(Offset(sliderCenterX, sliderCenterY))
                                } else {
                                    offsetX = 0f
                                }
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                val newOffset = (offsetX + dragAmount.x).coerceIn(0f, maxOffsetPx)
                                offsetX = newOffset
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Swipe to start",
                    tint = PrimaryColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
