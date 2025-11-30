package com.afsar.titipin.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.afsar.titipin.ui.theme.BackgroundLight

@Composable
fun CirclesImage(
    imageUrl: String?,
    size: Dp = 60.dp,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .crossfade(true)
            .build(),
        contentDescription = "Foto Profil",

        placeholder = rememberVectorPainter(Icons.Default.Person),
        error = rememberVectorPainter(Icons.Default.Person),

        contentScale = ContentScale.Crop,

        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .border(1.dp, BackgroundLight, CircleShape)
    )
}