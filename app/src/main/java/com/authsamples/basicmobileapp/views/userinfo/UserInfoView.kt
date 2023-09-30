package com.authsamples.basicmobileapp.views.userinfo

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

@Composable
fun UserInfoView(modifier: Modifier) {
    Text(
        text = "Demo User",
        textAlign = TextAlign.Right,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        color = Color(0xFF212121),
        modifier = modifier
    )
}