package com.example.titipin.ui.screen

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.titipin.ui.theme.*

@Composable
fun ReceiptScannerScreen(
    onBackClick: () -> Unit = {},
    onCaptureClick: () -> Unit = {}
) {
    var flashEnabled by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Camera preview placeholder (dark gray)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF2C2C2C))
        )
        
        // Scanning Frame
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(280.dp, 400.dp)
                .border(
                    width = 3.dp,
                    color = Color(0xFF00BCD4),
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            // Corner indicators
            CornerIndicator(Modifier.align(Alignment.TopStart), isTopLeft = true)
            CornerIndicator(Modifier.align(Alignment.TopEnd), isTopRight = true)
            CornerIndicator(Modifier.align(Alignment.BottomStart), isBottomLeft = true)
            CornerIndicator(Modifier.align(Alignment.BottomEnd), isBottomRight = true)
        }
        
        // Top Close Button
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .statusBarsPadding()
                .padding(16.dp)
                .align(Alignment.TopStart)
                .size(48.dp)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Close",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        
        // Bottom Instructions and Controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Align the receipt within the frame",
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 32.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Gallery Button
                IconButton(
                    onClick = { /* Gallery */ },
                    modifier = Modifier
                        .size(56.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = "Gallery",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                // Capture Button
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clickable { onCaptureClick() }
                        .background(Color.White, CircleShape)
                        .border(4.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .background(Color.White, CircleShape)
                    )
                }
                
                // Flash Toggle
                IconButton(
                    onClick = { flashEnabled = !flashEnabled },
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            if (flashEnabled) PrimaryColor else Color.White.copy(alpha = 0.2f),
                            CircleShape
                        )
                ) {
                    Icon(
                        if (flashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = "Flash",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CornerIndicator(
    modifier: Modifier,
    isTopLeft: Boolean = false,
    isTopRight: Boolean = false,
    isBottomLeft: Boolean = false,
    isBottomRight: Boolean = false
) {
    Box(
        modifier = modifier
            .padding(
                start = if (isTopLeft || isBottomLeft) 0.dp else 0.dp,
                end = if (isTopRight || isBottomRight) 0.dp else 0.dp,
                top = if (isTopLeft || isTopRight) 0.dp else 0.dp,
                bottom = if (isBottomLeft || isBottomRight) 0.dp else 0.dp
            )
    ) {
        Canvas(modifier = Modifier.size(30.dp)) {
            val strokeWidth = 6.dp.toPx()
            val length = 20.dp.toPx()
            
            // Horizontal line
            drawLine(
                color = Color(0xFF00BCD4),
                start = androidx.compose.ui.geometry.Offset(
                    if (isTopRight || isBottomRight) size.width - length else 0f,
                    if (isTopLeft || isTopRight) 0f else size.height
                ),
                end = androidx.compose.ui.geometry.Offset(
                    if (isTopLeft || isBottomLeft) length else size.width,
                    if (isTopLeft || isTopRight) 0f else size.height
                ),
                strokeWidth = strokeWidth
            )
            
            // Vertical line
            drawLine(
                color = Color(0xFF00BCD4),
                start = androidx.compose.ui.geometry.Offset(
                    if (isTopLeft || isBottomLeft) 0f else size.width,
                    if (isBottomLeft || isBottomRight) size.height - length else 0f
                ),
                end = androidx.compose.ui.geometry.Offset(
                    if (isTopLeft || isBottomLeft) 0f else size.width,
                    if (isTopLeft || isTopRight) length else size.height
                ),
                strokeWidth = strokeWidth
            )
        }
    }
}
