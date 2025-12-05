package com.afsar.titipin.ui.screens

import android.R.attr.onClick
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen() {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {  },
            ) {
                Icon(Icons.Filled.Add, "Floating action button.")
            }
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier.fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
        ) {

            Row (
                modifier = Modifier.fillMaxWidth()
            ){

                Text(
                    text = "Halo, ",
                    fontSize = 24.sp,
                    )

                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = "Muhammad Afsar T",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }


            Card (
                modifier = Modifier.fillMaxWidth()
                    .padding(0.dp,10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFFFFF)
                ),
                border = BorderStroke(1.dp, Color(0xFF000000))

            ){
                Column(
                    modifier = Modifier.padding(10.dp)
                        .padding(0.dp,10.dp)

                ) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = "Ballance",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = "Rp. 24.000,000",
                        fontSize = 34.sp
                    )
                }
            }

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "Active Session",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            LazyColumn {
                items(10) { index ->
                    Card(
                        modifier = Modifier.fillMaxWidth()
                            .padding(0.dp,10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFFFFF)
                        )
                    ) {
                        Column {
                            Text(text = "Session $index")
                            Text(text = "Description $index")
                        }
                    }
                }
            }





        }

    }
}


@Preview(showBackground = true)
@Composable
fun PreviewHomeScreen(){
    HomeScreen()

}