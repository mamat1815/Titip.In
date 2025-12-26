package com.afsar.titipin.ui.components.molecules

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.afsar.titipin.R
import com.afsar.titipin.ui.theme.OrangePrimary

@Composable
fun SessionProgressBar(
    currentStep: Int = 1,
    instructionText: String = "",
    iconRes: Int = 0,
    modifier: Modifier = Modifier
) {
    val steps = listOf(
        R.drawable.ic_listorder,
        R.drawable.ic_beli,
        R.drawable.ic_delivery,
        R.drawable.ic_delivered
    )


    Surface(
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.LightGray),
        color = Color.White,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            if (instructionText.isNotEmpty() && iconRes != 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Image(
                        painter = painterResource(id = iconRes),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = instructionText,
                        fontSize = 12.sp,
                        color = Color.Black,
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }


            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                steps.forEachIndexed { index, iconStepRes ->
                    val stepNum = index + 1
                    val isActive = stepNum <= currentStep
                    val isLast = index == steps.size - 1

                    StepIcon(iconRes = iconStepRes, isActive = isActive)

                    if (!isLast) {
                        Divider(
                            color = if (isActive) OrangePrimary else Color.LightGray,
                            thickness = 2.dp,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StepIcon(iconRes: Int, isActive: Boolean) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(32.dp)
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            colorFilter = if (isActive) ColorFilter.tint(OrangePrimary) else ColorFilter.tint(Color.LightGray)
        )
    }
}