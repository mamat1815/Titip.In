package com.afsar.titipin.ui.home.screens

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.afsar.titipin.ui.components.CirclesImage
import com.afsar.titipin.ui.components.ProfileMenuItem
import com.afsar.titipin.ui.circle.AddCircleActivity
import com.afsar.titipin.ui.circle.CircleActivity
import com.afsar.titipin.ui.theme.TextDarkSecondary
import com.afsar.titipin.ui.theme.TextLightPrimary
import com.afsar.titipin.ui.theme.jakartaFamily
import com.afsar.titipin.ui.home.viewmodel.ProfileViewModel


@Composable
fun ProfileScreen(viewModel: ProfileViewModel = hiltViewModel()) {
    val context = LocalContext.current
    Column(
        modifier = Modifier.fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(
            modifier = Modifier.height(16.dp)
        )
        CirclesImage(
            imageUrl = viewModel.currentUser?.photoUrl,
            size = 120.dp,
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text = viewModel.currentUser?.name ?: "Testing",
            fontFamily = jakartaFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = TextLightPrimary
        )
        val username = "@" + viewModel.currentUser?.username

        Text(
            text = username,
            fontFamily = jakartaFamily,
            fontWeight = FontWeight.Thin,
            fontSize = 18.sp,
            color = TextDarkSecondary
        )

        Row {
            Button(
                onClick = { /*TODO*/ },
                modifier = Modifier.weight(1f),
            ) { }
            Button(
                onClick = { /*TODO*/ },
                modifier = Modifier.weight(1f),
            ) { }
        }

        Card(
            modifier = Modifier.padding(16.dp)
        ) {

        }

        ProfileMenuItem(
            icon = Icons.Default.Group,
            text = "Circle Saya",
            onClick = {
                val intent = Intent(context, CircleActivity::class.java)
                context.startActivity(intent) }
        )

    }

}