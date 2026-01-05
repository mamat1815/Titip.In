package com.afsar.titipin.ui.components.molecules

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
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
    onStepClick: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val steps = listOf(
//        R.drawable.ic_listorder,
//        R.drawable.ic_beli,
//        R.drawable.ic_delivery,
//        R.drawable.ic_delivered
        Icons.Default.Checklist,
        Icons.Default.ShoppingBag,
        Icons.Default.Payment
    )

    Surface(
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
        color = Color.White,
        modifier = modifier.fillMaxWidth(),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            if (instructionText.isNotEmpty() && iconRes != 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(OrangePrimary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = iconRes),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = instructionText,
                        fontSize = 12.sp,
                        color = Color.DarkGray,
                        lineHeight = 16.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))
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

                    StepIcon(
                        iconRes = iconStepRes,
                        isActive = isActive,
                        onClick = { onStepClick(stepNum) }
                    )

                    if (!isLast) {
                        val animatedColor by animateColorAsState(
                            targetValue = if (isActive && currentStep > stepNum) OrangePrimary else Color.LightGray,
                            animationSpec = tween(500),
                            label = "color"
                        )

                        HorizontalDivider(
                            color = animatedColor,
                            thickness = 2.dp,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StepIcon(
    iconRes: ImageVector,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val iconColor by animateColorAsState(
        targetValue = if (isActive) OrangePrimary else Color.LightGray,
        animationSpec = tween(500),
        label = "iconColor"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .clickable { onClick() }
            .background(if (isActive) OrangePrimary.copy(alpha = 0.1f) else Color.Transparent)
    ) {
        Image(
            imageVector = iconRes,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            colorFilter = ColorFilter.tint(iconColor)
        )
    }
}