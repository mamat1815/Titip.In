package com.afsar.titipin.ui.session

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@Composable
fun LocationPickerDialog(
    onDismiss: () -> Unit,
    onLocationSelected: (LatLng) -> Unit
) {
    // Lokasi Default (Misal: Monas Jakarta)
//    TODO LIVE LOCATION untuk default location
    val defaultLocation = LatLng(-6.175392, 106.827153)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 15f)
    }


    // State untuk menyimpan posisi tengah layar saat ini
    var centerLocation by remember { mutableStateOf(defaultLocation) }

    // Update centerLocation setiap kamera bergerak
    LaunchedEffect(cameraPositionState.isMoving) {
        if (!cameraPositionState.isMoving) {
            centerLocation = cameraPositionState.position.target
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false) // Full width
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp) // Tinggi Map
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // 1. GOOGLE MAP
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    uiSettings = MapUiSettings(zoomControlsEnabled = false)
                )

                // 2. PIN MARKER (Selalu di Tengah)
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Pin",
                    tint = Color(0xFFD32F2F), // Merah
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.Center)
                        .offset(y = (-24).dp) // Offset biar ujung pin pas di tengah
                )

                // 3. BUTTON PILIH
                Button(
                    onClick = { onLocationSelected(centerLocation) },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF370061))
                ) {
                    Text("Pilih Lokasi Ini")
                }
            }
        }
    }
}