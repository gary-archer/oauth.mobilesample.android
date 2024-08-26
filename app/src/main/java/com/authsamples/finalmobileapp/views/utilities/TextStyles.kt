package com.authsamples.finalmobileapp.views.utilities

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

/*
 * Shared text styles
 */
object TextStyles {

    val header = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        color = CustomColors.value,
    )

    val sessionId = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        color = CustomColors.label,
    )

    val label = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = CustomColors.label,
    )

    val value = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = CustomColors.value,
    )

    val money = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = CustomColors.green,
    )

    val error = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = CustomColors.error,
    )

    val tooltip = TextStyle(
        fontSize = 14.sp,
        textAlign = TextAlign.Center
    )

    val headerButton = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        textAlign = TextAlign.Center
    )
}
